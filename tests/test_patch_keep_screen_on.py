import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from patch_keep_screen_on import modify, CALL

SMALI = (
    ".class public Lcom/nianticproject/holoholo/libholoholo/unity/UnityMainActivity;\n"
    ".super Lcom/unity3d/player/UnityPlayerActivity;\n"
    ".method public onResume()V\n"
    "    .locals 1\n"
    "    invoke-super {p0}, Lcom/unity3d/player/UnityPlayerActivity;->onResume()V\n"
    "    return-void\n.end method\n"
    ".method public onWindowFocusChanged(Z)V\n"
    "    .locals 0\n"
    "    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->onWindowFocusChanged(Z)V\n"
    "    if-eqz p1, :cond_0\n"
    "    :cond_0\n"
    "    return-void\n.end method\n"
)

class KeepScreenOnPatchTests(unittest.TestCase):
    def test_adds_exactly_two_hooks_without_extra_menu(self):
        patched, status = modify(SMALI)
        self.assertEqual(status, "aplicado")
        self.assertEqual(patched.count(CALL), 2)
        self.assertIn("const/16 v1, 0x80", patched)
        self.assertIn("->addFlags(I)V", patched)
        self.assertNotIn("FavoritesOverlay;->attach", patched)

    def test_idempotent(self):
        once, _ = modify(SMALI)
        twice, status = modify(once)
        self.assertEqual(once, twice)
        self.assertEqual(status, "já aplicado")

    def test_rejects_incompatible_activity(self):
        with self.assertRaises(ValueError):
            modify(SMALI.replace("onWindowFocusChanged(Z)V", "onWindowGone(Z)V"))

if __name__ == "__main__":
    unittest.main()
