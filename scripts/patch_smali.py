#!/usr/bin/env python3
"""Adiciona gancho para painel local sem alterar login, licença ou rede."""
import argparse
import re
from pathlib import Path
TARGET = Path("com/nianticproject/holoholo/libholoholo/unity/UnityMainActivity.smali")
HOOK = "    invoke-static {p0}, Lcom/pgs/personal/FavoritesOverlay;->attach(Landroid/app/Activity;)V"

def modify(text):
    if HOOK in text: return text, "ja_injetado"
    if not re.search(r"^\.class\s+.*?Lcom/nianticproject/holoholo/libholoholo/unity/UnityMainActivity;$",text,re.M):
        raise ValueError("A classe não é UnityMainActivity")
    found=re.search(r"(^\.method[^\n]*\sonCreate\(Landroid/os/Bundle;\)V[^\n]*\n)(.*?)(^\.end method\s*$)",text,re.M|re.S)
    if found:
        body=found.group(2)
        sup=re.search(r"^\s*invoke-super(?:/range)?\s+\{[^\n}]+\},\s+[^\n]+->onCreate\(Landroid/os/Bundle;\)V[^\n]*$",body,re.M)
        if not sup:raise ValueError("onCreate não contém invoke-super esperado")
        pos=found.start(2)+sup.end()
        return text[:pos]+"\n"+HOOK+text[pos:], "onCreate_existente"
    base=re.search(r"^\.super\s+(L[^\s;]+;)",text,re.M)
    if not base:raise ValueError("Superclasse ausente")
    extra=("\n.method public onCreate(Landroid/os/Bundle;)V\n"
           "    .locals 0\n"
           "    invoke-super {p0, p1}, "+base.group(1)+"->onCreate(Landroid/os/Bundle;)V\n"
           +HOOK+"\n    return-void\n.end method\n")
    return text+extra, "novo_onCreate"

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("decoded",type=Path)
    args=p.parse_args()
    files=list(args.decoded.glob("smali*/"+str(TARGET)))
    if len(files)!=1:raise SystemExit("UnityMainActivity.smali não encontrada unicamente")
    path=files[0]
    before=path.read_text("utf-8")
    after,mode=modify(before)
    if before!=after:path.write_text(after,encoding="utf-8")
    print("Patch aplicado:",mode)
