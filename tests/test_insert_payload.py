"""Regressões do empacotador DEX: testes sem APK de terceiros nem dados privados."""
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from insert_payload import next_dex_name, insert

class DexPackagingTests(unittest.TestCase):
    def test_next_available_slot(self):
        self.assertEqual(next_dex_name({"classes.dex", "classes2.dex", "classes3.dex"}), "classes4.dex")
        self.assertEqual(next_dex_name({"classes.dex", "classes2.dex", "classes3.dex", "classes4.dex"}), "classes5.dex")

    def test_reject_missing_dex(self):
        for names in ({"classes2.dex"}, {"classes.dex", "classes3.dex"}, set()):
            with self.assertRaises(ValueError):
                next_dex_name(names)

    def test_preserve_entries_when_adding_dex(self):
        for total in (3, 4):
            with self.subTest(original_dex_count=total), tempfile.TemporaryDirectory() as td:
                base = Path(td) / "original.apk"
                dex = Path(td) / "module.dex"
                out = Path(td) / "modified.apk"
                dex.write_bytes(b"dex" + bytes([10]) + b"035" + bytes([0]) + b"module")
                with zipfile.ZipFile(base, "w") as z:
                    z.writestr("AndroidManifest.xml", b"synthetic manifest")
                    z.writestr("assets/config.txt", b"keep this")
                    for idx in range(1, total + 1):
                        name = "classes.dex" if idx == 1 else f"classes{idx}.dex"
                        z.writestr(name, b"dex" + bytes([10]) + b"mock")
                slot = insert(base, dex, out)
                self.assertEqual(slot, f"classes{total + 1}.dex")
                with zipfile.ZipFile(out) as z:
                    self.assertIsNone(z.testzip())
                    self.assertEqual(z.read("assets/config.txt"), b"keep this")
                    self.assertEqual(z.read(slot), dex.read_bytes())

    def test_refuse_input_overwrite(self):
        with tempfile.TemporaryDirectory() as td:
            input_file = Path(td) / "one.apk"
            dex = Path(td) / "x.dex"
            dex.write_bytes(b"dex" + bytes([10]) + b"fake")
            with self.assertRaises(ValueError):
                insert(input_file, dex, input_file)

if __name__ == "__main__":
    unittest.main()
