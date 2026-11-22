#!/usr/bin/env bash
# Compilação experimental do patch local. Requer base de APK e assinatura do proprietário.
set -euo pipefail
BASE_APK="$1"
OUT_APK="$2"
ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK_PLATFORM="$ANDROID_HOME/platforms/android-35/android.jar"
TOOLS="$ANDROID_HOME/build-tools/35.0.0"
WORK="$RUNNER_TEMP/pgs-personal-build"
: "$APKTOOL_JAR"
: "$PGSHARP_KEYSTORE_PATH"
: "$PGSHARP_SIGNING_PASSWORD"
: "$PGSHARP_SIGNING_ALIAS"
[[ -f "$BASE_APK" && -f "$SDK_PLATFORM" && -f "$APKTOOL_JAR" && -f "$PGSHARP_KEYSTORE_PATH" ]] || { echo "Faltam ferramentas ou APK base"; exit 1; }
[[ -f "$ROOT/src/com/pgs/personal/FavoritesOverlay.java" && -f "$ROOT/assets/editor.html" ]] || { echo "Fonte incompleta: copie a classe Java e editor HTML do pacote de fonte para o repositório privado"; exit 1; }
mkdir -p "$WORK/classes" "$WORK/dex" "$(dirname "$OUT_APK")"
java -jar "$APKTOOL_JAR" d -f -r "$BASE_APK" -o "$WORK/decoded"
python3 "$ROOT/scripts/patch_smali.py" "$WORK/decoded"
mkdir -p "$WORK/decoded/assets/pgs_private"
cp "$ROOT/assets/editor.html" "$WORK/decoded/assets/pgs_private/editor.html"
javac -encoding UTF-8 --release 8 -cp "$SDK_PLATFORM" -d "$WORK/classes" "$ROOT/src/com/pgs/personal/FavoritesOverlay.java"
"$TOOLS/d8" --min-api 26 --lib "$SDK_PLATFORM" --output "$WORK/dex" "$WORK/classes/com/pgs/personal/"*.class
java -jar "$APKTOOL_JAR" b "$WORK/decoded" -o "$WORK/unsigned.apk"
python3 "$ROOT/scripts/insert_payload.py" "$WORK/unsigned.apk" "$WORK/dex/classes.dex" "$WORK/with-dex.apk"
"$TOOLS/zipalign" -f -P 16 4 "$WORK/with-dex.apk" "$WORK/aligned.apk"
"$TOOLS/apksigner" sign --ks "$PGSHARP_KEYSTORE_PATH" --ks-key-alias "$PGSHARP_SIGNING_ALIAS" --ks-pass env:PGSHARP_SIGNING_PASSWORD --key-pass env:PGSHARP_SIGNING_PASSWORD --out "$OUT_APK" "$WORK/aligned.apk"
"$TOOLS/apksigner" verify --verbose "$OUT_APK"
python3 - "$WORK/unsigned.apk" "$OUT_APK" "$ROOT/scripts" <<'PY'
import sys,zipfile
sys.path.insert(0,sys.argv[3])
from insert_payload import next_dex_name
with zipfile.ZipFile(sys.argv[1]) as original, zipfile.ZipFile(sys.argv[2]) as result:
    assert result.testzip() is None, "Falha de integridade ZIP"
    files=set(result.namelist())
    slot=next_dex_name(original.namelist())
    assert "AndroidManifest.xml" in files, "Manifest ausente"
    assert slot in files, f"DEX do painel ausente: {slot}"
    assert "assets/pgs_private/editor.html" in files, "Editor HTML ausente"
    assert result.read(slot).startswith(b"dex"+bytes([10])), "DEX corrompido"
print("Estrutura do APK recomposto conferida; próximo DEX preservado.")
PY
echo "APK assinado para testes, ainda não validado em dispositivo."
