import unittest,sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/"scripts"))
from patch_smali import modify,HOOK
H=".class public Lcom/nianticproject/holoholo/libholoholo/unity/UnityMainActivity;\n.super Lcom/unity3d/player/UnityPlayerActivity;\n"
class PatchTests(unittest.TestCase):
    def test_hook_existing(self):
        s=H+".method protected onCreate(Landroid/os/Bundle;)V\n    .locals 0\n    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->onCreate(Landroid/os/Bundle;)V\n    return-void\n.end method\n"
        text,mode=modify(s)
        self.assertEqual(mode,"onCreate_existente")
        self.assertIn(HOOK,text)
        self.assertEqual(modify(text)[1],"ja_injetado")
    def test_hook_new(self):
        text,mode=modify(H)
        self.assertEqual(mode,"novo_onCreate")
        self.assertIn(HOOK,text)
    def test_fail_wrong_class(self):
        with self.assertRaises(ValueError):modify(".class public Lwrong;\n.super Landroid/app/Activity;")
if __name__=="__main__":unittest.main()
