"""Exercise credential failures and the legal publication gate without external services."""
import base64
from contextlib import redirect_stdout
from html.parser import HTMLParser
import io
import json
import os
from pathlib import Path
import stat
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))
import build_legal_site
import check_legal_site
import prepare_release


class Links(HTMLParser):
    def __init__(self):
        super().__init__()
        self.urls = []
        self.scripts = []

    def handle_starttag(self, tag, attributes):
        values = dict(attributes)
        self.urls.extend(values[key] for key in ('href', 'src') if key in values)
        if tag == 'script':
            self.scripts.append(values)


class ReleasePreparationTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.original_directory = Path.cwd()
        os.chdir(self.directory)
        self.addCleanup(os.chdir, self.original_directory)
        (self.directory / 'androidApp').mkdir()
        self.secret = 'test-private-value-do-not-print'
        self.keystore = b'not-a-real-keystore' * 16
        self.environment = {
            'RUNNER_TEMP': str(self.directory / 'runner'),
            'LOOM_VERSION_NAME': '4.0.0',
            'LOOM_UPLOAD_STORE_PASSWORD': self.secret,
            'LOOM_UPLOAD_KEY_PASSWORD': self.secret,
            'LOOM_UPLOAD_KEY_ALIAS': 'test-upload',
            'LOOM_KEYSTORE_BASE64': base64.encodebytes(self.keystore).decode(),
            'LOOM_FIREBASE_CONFIG': json.dumps({'project_info': {'project_number': '123'}, 'client': [
                {'client_info': {'mobilesdk_app_id': '1:123:android:abc123', 'android_client_info': {'package_name': 'com.eyther.lumbridge'}}},
            ]}),
            'LOOM_PLAY_SERVICE_ACCOUNT': json.dumps({
                'type': 'service_account',
                'client_email': 'test@loom-test.iam.gserviceaccount.com',
                'private_key': self.secret,
            }),
        }

    def test_preparation_accepts_wrapped_base64_and_limits_private_file_permissions(self):
        output = io.StringIO()
        with patch.dict(os.environ, self.environment, clear=True), redirect_stdout(output):
            prepare_release.prepare()
        runner = self.directory / 'runner'
        self.assertEqual(self.keystore, (runner / 'loom-upload.jks').read_bytes())
        for name in ('loom-upload.jks', 'loom-play-service-account.json'):
            self.assertEqual(0o600, stat.S_IMODE((runner / name).stat().st_mode))
        self.assertEqual({'firebaseApp': 'projects/123/apps/1:123:android:abc123'}, json.loads((runner / 'loom-release-config.json').read_text()))
        self.assertNotIn(self.secret, output.getvalue())
        self.assertNotIn(self.environment['LOOM_KEYSTORE_BASE64'], output.getvalue())

    def test_missing_store_password_fails_before_writing_credentials(self):
        del self.environment['LOOM_UPLOAD_STORE_PASSWORD']
        with patch.dict(os.environ, self.environment, clear=True):
            with self.assertRaisesRegex(ValueError, 'LOOM_UPLOAD_STORE_PASSWORD'):
                prepare_release.prepare()
        self.assertFalse((self.directory / 'runner/loom-upload.jks').exists())

    def test_version_and_app_identity_are_validated(self):
        for version in ('0', '2100000001', '1; invalid-command'):
            with self.subTest(version=version):
                with patch.dict(os.environ, {**self.environment, 'LOOM_VERSION_CODE': version}, clear=True):
                    with self.assertRaisesRegex(ValueError, 'version code'):
                        prepare_release.prepare()
        with patch.dict(os.environ, {**self.environment, 'LOOM_FIREBASE_CONFIG': '{"client": []}'}, clear=True):
            with self.assertRaisesRegex(ValueError, 'matching Loom Android client'):
                prepare_release.prepare()

    def test_malformed_private_json_is_not_echoed_by_the_cli(self):
        environment = {**os.environ, **self.environment, 'LOOM_PLAY_SERVICE_ACCOUNT': self.secret}
        result = subprocess.run(
            [sys.executable, str(TOOLS / 'prepare_release.py')],
            env=environment, capture_output=True, text=True, check=False,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn('not valid JSON', result.stderr)
        self.assertNotIn(self.secret, result.stderr + result.stdout)


class LegalPublicationTest(unittest.TestCase):
    def test_pages_are_tracker_free_and_all_local_links_resolve(self):
        with tempfile.TemporaryDirectory() as directory, redirect_stdout(io.StringIO()):
            output = Path(directory)
            build_legal_site.build(output)
            for path in output.rglob('*.html'):
                links = Links()
                body = path.read_text()
                links.feed(body)
                self.assertFalse(links.scripts)
                self.assertIn('eytherdeveloper@gmail.com', body)
                for url in links.urls:
                    if url.startswith('/'):
                        target = output / url.lstrip('/')
                        self.assertTrue(target.exists(), f'{path.name}: {url}')
            release = json.loads((output / 'release.json').read_text())
            self.assertEqual('Loom', release['publisher'])
            self.assertNotIn('country', release)
            self.assertFalse(release['draft'])

    def test_live_gate_rejects_draft_wrong_app_and_stale_individual_pages(self):
        config = build_legal_site.configuration()
        release = {'app': 'Loom', 'package': 'com.eyther.lumbridge',
                   'publisher': config['PUBLISHER_NAME'], 'version': config['POLICY_VERSION'], 'draft': False}
        for changes in ({'draft': True}, {'package': 'another.app'}, {'version': '2000-01-01'}):
            with self.subTest(changes=changes):
                with patch.object(check_legal_site.urllib.request, 'urlopen',
                                  return_value=io.BytesIO(json.dumps({**release, **changes}).encode())):
                    with self.assertRaises(ValueError):
                        check_legal_site.check(live=True)
        responses = [io.BytesIO(json.dumps(release).encode()), io.BytesIO(
            b'<meta name="loom-policy-version" content="2000-01-01">eytherdeveloper@gmail.com')]
        with patch.object(check_legal_site.urllib.request, 'urlopen', side_effect=responses):
            with self.assertRaisesRegex(ValueError, 'privacy page is incomplete'):
                check_legal_site.check(live=True)


if __name__ == '__main__':
    unittest.main()
