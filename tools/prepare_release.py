#!/usr/bin/env python3
"""Validate release metadata and materialize masked CI credentials outside the checkout."""
import base64
import json
import os
from pathlib import Path
import re

PACKAGE_NAME = 'com.eyther.lumbridge'


def require(name):
    value = os.environ.get(name, '')
    if not value:
        raise ValueError(f'Missing release setting: {name}')
    return value


def prepare():
    version_code = os.environ.get('LOOM_VERSION_CODE', '')
    version_name = os.environ.get('LOOM_VERSION_NAME', '')
    if version_code and (not re.fullmatch(r'[0-9]+', version_code) or not 1 <= int(version_code) <= 2_100_000_000):
        raise ValueError('Use a positive Play version code below 2100000001')
    if version_name and not re.fullmatch(r'[0-9]+\.[0-9]+\.[0-9]+', version_name):
        raise ValueError('Invalid release version name')

    for name in ('LOOM_UPLOAD_STORE_PASSWORD', 'LOOM_UPLOAD_KEY_PASSWORD', 'LOOM_UPLOAD_KEY_ALIAS'):
        require(name)

    config = json.loads(require('LOOM_FIREBASE_CONFIG'))
    if not isinstance(config, dict):
        raise ValueError('Firebase configuration must contain the complete JSON object')
    clients = config.get('client', [])
    if not isinstance(clients, list) or not any(
        isinstance(client, dict)
        and client.get('client_info', {}).get('android_client_info', {}).get('package_name') == PACKAGE_NAME
        for client in clients
    ):
        raise ValueError('Firebase configuration has no matching Loom Android client')

    matching = next(client for client in clients
                    if client.get('client_info', {}).get('android_client_info', {}).get('package_name') == PACKAGE_NAME)
    project_number = str(config.get('project_info', {}).get('project_number', ''))
    app_id = matching.get('client_info', {}).get('mobilesdk_app_id', '')
    if not re.fullmatch(r'[0-9]+', project_number) or not re.fullmatch(r'1:' + re.escape(project_number) + r':android:[a-f0-9]+', app_id):
        raise ValueError('Firebase configuration is missing its Android app ID or project number')

    temporary = Path(require('RUNNER_TEMP'))
    temporary.mkdir(parents=True, exist_ok=True)
    try:
        keystore = base64.b64decode(''.join(require('LOOM_KEYSTORE_BASE64').split()), validate=True)
    except ValueError as error:
        raise ValueError('The upload keystore secret must contain the complete Base64-encoded file') from error
    if len(keystore) < 128:
        raise ValueError('The upload keystore is empty or truncated')

    credential_text = os.environ.get('LOOM_PLAY_SERVICE_ACCOUNT', '')
    if not credential_text:
        raise ValueError('Missing Play service-account secret')
    account = json.loads(credential_text)
    if (not isinstance(account, dict) or account.get('type') != 'service_account'
            or not account.get('private_key') or not account.get('client_email')):
        raise ValueError('Use a Google service-account key, with Play Console and Firebase App Distribution permissions enabled')
    path = temporary / 'loom-play-service-account.json'
    path.write_text(credential_text)
    path.chmod(0o600)

    path = temporary / 'loom-upload.jks'
    path.write_bytes(keystore)
    path.chmod(0o600)
    Path('androidApp/google-services.json').write_text(json.dumps(config))
    (temporary / 'loom-release-config.json').write_text(json.dumps({'firebaseApp': f'projects/{project_number}/apps/{app_id}'}))
    print('Prepared signing and distribution configuration; release versions will be allocated automatically')


if __name__ == '__main__':
    try:
        prepare()
    except json.JSONDecodeError:
        raise SystemExit('A Firebase or Play service-account secret is not valid JSON. Paste the complete, unchanged JSON file.')
    except (ValueError, OSError) as error:
        raise SystemExit(str(error))
