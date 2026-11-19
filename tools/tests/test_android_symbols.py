"""Protect release symbol/package identity checks against stale or mismatched outputs."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import verify_android_symbols


class AndroidSymbolsTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        directory = Path(temporary.name)
        self.mapping = directory / 'mapping.txt'
        self.mapping.write_text('# compiler: R8\n# compiler_version: 9.4.24\n# pg_map_id: ' + 'a' * 64 + '\nOriginal -> a:\n')
        self.identifier = directory / 'mappingFileId.txt'
        self.identifier.write_text('b' * 32)
        self.resource = directory / 'mappingfileid.xml'
        self.resource.write_text('<resources><string name="com.google.firebase.crashlytics.mapping_file_id">' + 'b' * 32 + '</string></resources>')
        self.apk = directory / 'Loom.apk'
        with zipfile.ZipFile(self.apk, 'w') as archive:
            archive.writestr('classes.dex', b'R8 source marker: r8-map-id-' + b'a' * 64)
        self.resources = 'resource 0x1 string/com.google.firebase.crashlytics.mapping_file_id\n      () "' + 'b' * 32 + '"\n'
        self.badging = "package: name='com.eyther.lumbridge' versionCode='63' versionName='4.0.13'\n"

    def verify(self):
        return verify_android_symbols.verify(
            mapping=self.mapping, generated_id=self.identifier, resource=self.resource,
            apk=self.apk, aapt2=Path('/unused/aapt2'), version_name='4.0.13', version_code='63',
            source_commit='c' * 40,
        )

    def test_a_matching_apk_records_both_mapping_ids_and_content_hashes(self):
        with patch.object(verify_android_symbols.subprocess, 'check_output', side_effect=[self.resources, self.badging]):
            result = self.verify()
        self.assertEqual('a' * 64, result['r8MapId'])
        self.assertEqual('b' * 32, result['crashlyticsMappingId'])
        self.assertEqual('c' * 40, result['sourceCommit'])
        self.assertEqual(63, result['versionCode'])
        self.assertEqual(verify_android_symbols.sha256(self.apk), result['apkSha256'])
        self.assertEqual(verify_android_symbols.sha256(self.mapping), result['mappingSha256'])
        json.dumps(result)

    def test_a_stale_packaged_mapping_id_or_wrong_version_blocks_distribution(self):
        for resources, badging in (
            (self.resources.replace('b' * 32, 'd' * 32), self.badging),
            (self.resources, self.badging.replace("versionCode='63'", "versionCode='62'")),
        ):
            with self.subTest(resources=resources, badging=badging):
                with patch.object(verify_android_symbols.subprocess, 'check_output', side_effect=[resources, badging]):
                    with self.assertRaises(ValueError):
                        self.verify()

    def test_missing_r8_identity_or_inconsistent_generated_resources_fail_before_packaging_checks(self):
        self.resource.write_text('<resources/>')
        with self.assertRaisesRegex(ValueError, 'differs'):
            self.verify()
        self.mapping.write_text('Original -> a:\n')
        with self.assertRaisesRegex(ValueError, 'R8 map ID'):
            self.verify()

    def test_a_mapping_from_another_build_cannot_be_attached_to_this_apk(self):
        self.mapping.write_text('# pg_map_id: ' + 'e' * 64 + '\nOriginal -> a:\n')
        with self.assertRaisesRegex(ValueError, 'does not match the code'):
            self.verify()


if __name__ == '__main__':
    unittest.main()
