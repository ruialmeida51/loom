"""Keep the native callback registrations aligned with the authentication adapter."""
from pathlib import Path
import plistlib
import re
import tomllib
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
ANDROID = '{http://schemas.android.com/apk/res/android}'


class AuthCallbackConfigurationTest(unittest.TestCase):
    def test_android_ios_and_local_auth_register_the_same_callback(self):
        source = ROOT / 'shared/features/authentication/src/commonMain/kotlin/com/eyther/lumbridge/features/authentication/data/network/config/AuthenticationRedirect.kt'
        values = dict(re.findall(r'const val (SCHEME|HOST|PATH) = "([^"]+)"', source.read_text()))
        url = f"{values['SCHEME']}://{values['HOST']}{values['PATH']}"

        manifest = ET.parse(ROOT / 'androidApp/src/main/AndroidManifest.xml')
        matches = []
        for intent in manifest.findall('.//activity/intent-filter'):
            for data in intent.findall('data'):
                if data.get(ANDROID + 'scheme') == values['SCHEME']:
                    matches.append(intent)
                    self.assertEqual(values['HOST'], data.get(ANDROID + 'host'))
                    self.assertEqual(values['PATH'], data.get(ANDROID + 'path'))
                    self.assertIn('android.intent.action.VIEW', [a.get(ANDROID + 'name') for a in intent.findall('action')])
                    self.assertIn('android.intent.category.BROWSABLE', [c.get(ANDROID + 'name') for c in intent.findall('category')])
        self.assertEqual(1, len(matches))

        with (ROOT / 'iosApp/iosApp/Info.plist').open('rb') as file:
            info = plistlib.load(file)
        schemes = [scheme for entry in info['CFBundleURLTypes'] for scheme in entry['CFBundleURLSchemes']]
        self.assertIn(values['SCHEME'], schemes)

        config = tomllib.loads((ROOT / 'supabase/config.toml').read_text())
        self.assertIn(url, config['auth']['additional_redirect_urls'])
