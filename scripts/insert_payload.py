#!/usr/bin/env python3
"""Inclui um DEX de módulo próprio no próximo slot disponível do APK recomposto."""
import argparse
import re
import zipfile
from pathlib import Path

def next_dex_name(filenames):
    """Exige uma sequência contínua e escolhe classesN.dex sem sobrescrever DEX existente."""
    filenames = set(filenames)
    slots = set()
    for filename in filenames:
        if filename == "classes.dex":
            slots.add(1)
        else:
            match = re.fullmatch(r"classes([2-9][0-9]*)\.dex", filename)
            if match:
                slots.add(int(match.group(1)))
    if 1 not in slots or slots != set(range(1, max(slots) + 1)):
        raise ValueError("Sequência DEX incompleta ou inesperada")
    new_slot = max(slots) + 1
    if new_slot > 99:
        raise ValueError("Muitos arquivos DEX: revisar APK manualmente")
    name = f"classes{new_slot}.dex"
    if name in filenames:
        raise ValueError("Novo DEX colidiria com arquivo existente")
    return name

def insert(apk: Path, dex: Path, output: Path):
    if apk.resolve() == output.resolve():
        raise ValueError("O arquivo de saída não pode sobrescrever o APK de entrada")
    if dex.read_bytes()[:4] != b"dex\n":
        raise ValueError("DEX inválido")
    with zipfile.ZipFile(apk) as src:
        entries = src.namelist()
        if len(entries) != len(set(entries)):
            raise ValueError("APK contém nomes de entrada duplicados")
        found = set(entries)
        if "AndroidManifest.xml" not in found:
            raise ValueError("Manifest ausente")
        target = next_dex_name(found)
        with zipfile.ZipFile(output, "w", allowZip64=True) as out:
            for item in src.infolist():
                if item.filename.startswith("META-INF/") and item.filename.upper().endswith((".SF", ".RSA", ".DSA", ".EC")):
                    continue
                out.writestr(item, src.read(item.filename))
            out.write(dex, arcname=target, compress_type=zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(output) as z:
        if z.testzip() is not None or target not in z.namelist():
            raise ValueError("Falha de integridade do ZIP ou DEX ausente")
    return target

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path)
    parser.add_argument("dex", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    try:
        slot = insert(args.apk, args.dex, args.output)
    except (ValueError, zipfile.BadZipFile) as exc:
        raise SystemExit(str(exc)) from exc
    print(f"DEX próprio incluído como {slot}; pacote ainda não assinado")
