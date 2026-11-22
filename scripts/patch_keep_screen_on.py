#!/usr/bin/env python3
"""Ativa FLAG_KEEP_SCREEN_ON no Activity principal; não cria janelas ou menus."""
from pathlib import Path
import argparse

TARGET = Path("com/nianticproject/holoholo/libholoholo/unity/UnityMainActivity.smali")
CLS = "Lcom/nianticproject/holoholo/libholoholo/unity/UnityMainActivity;"
CALL = f"    invoke-direct {{p0}}, {CLS}->keepScreenAwake()V"
METHOD = f"""
# Sem menus flutuantes: aplica FLAG_KEEP_SCREEN_ON somente à Window ativa.
.method private keepScreenAwake()V
    .locals 2
    invoke-virtual {{p0}}, {CLS}->getWindow()Landroid/view/Window;
    move-result-object v0
    const/16 v1, 0x80
    invoke-virtual {{v0, v1}}, Landroid/view/Window;->addFlags(I)V
    return-void
.end method
"""

def modify(text):
    if ".method private keepScreenAwake()V" in text:
        if text.count(CALL) != 2:
            raise ValueError("Patch parcial ou incompatível")
        return text, "já aplicado"
    if "Lcom/pgs/personal/FavoritesOverlay;->attach(" in text:
        raise ValueError("Este patch não pode distribuir o editor flutuante antigo")
    for method_name, anchor in (
        ("onResume()V", "    invoke-super {p0}, Lcom/unity3d/player/UnityPlayerActivity;->onResume()V"),
        ("onWindowFocusChanged(Z)V", "    if-eqz p1, :cond_0"),
    ):
        heading = ".method public " + method_name
        start = text.find(heading)
        if start < 0:
            raise ValueError("Método não encontrado: " + method_name)
        end = text.find(".end method", start)
        if end < 0 or text[start:end].count(anchor) != 1:
            raise ValueError("Método incompatível: " + method_name)
        idx = text.index(anchor, start, end) + len(anchor)
        text = text[:idx] + "\n" + CALL + text[idx:]
    text += "\n" + METHOD
    if text.count(CALL) != 2:
        raise AssertionError("O patch deveria instalar dois pontos de ativação")
    return text, "aplicado"

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("decoded", type=Path)
    args = parser.parse_args()
    matches = list(args.decoded.glob("smali*/" + str(TARGET)))
    if len(matches) != 1:
        parser.error("UnityMainActivity.smali não localizada unicamente")
    path = matches[0]
    before = path.read_text(encoding="utf-8")
    after, result = modify(before)
    if after != before:
        path.write_text(after, encoding="utf-8")
    print("Tela ativa:", result)
