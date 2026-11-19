"""Verify and identify the exact symbols/APK being distributed, without accessing credentials."""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile


def sha256(path):
    with path.open('rb') as data:
        return hashlib.file_digest(data, 'sha256').hexdigest()


def verify(mapping, generated_id, resource, apk, aapt2, version_name, version_code, source_commit):
    header = {}
    with mapping.open() as lines:
        for line in lines:
            if not line.startswith('#'):
                break
            if ': ' in line:
                key, value = line[2:].strip().split(': ', 1)
                header[key] = value
    r8_id = header.get('pg_map_id', '')
    if not re.fullmatch(r'[a-fA-F0-9]{7,64}', r8_id):
        raise ValueError('The final release mapping is missing its R8 map ID')
    with zipfile.ZipFile(apk) as archive:
        dex_names = [name for name in archive.namelist() if re.fullmatch(r'classes\d*\.dex', name)]
        marker = ('r8-map-id-' + r8_id).encode('ascii')
        if not any(marker in archive.read(name) for name in dex_names):
            raise ValueError('The R8 mapping does not match the code packaged in this APK')
    crashlytics_id = generated_id.read_text().strip()
    if not re.fullmatch(r'[a-fA-F0-9]{32}', crashlytics_id):
        raise ValueError('Missing or invalid Crashlytics mapping ID')
    resource_id = ET.parse(resource).find("string[@name='com.google.firebase.crashlytics.mapping_file_id']")
    if resource_id is None or resource_id.text != crashlytics_id:
        raise ValueError('Crashlytics ID differs between generated metadata and Android resources')
    resources = subprocess.check_output([str(aapt2), 'dump', 'resources', str(apk)], text=True)
    match = re.search(
        r'string/com\.google\.firebase\.crashlytics\.mapping_file_id\s+\(\) "([a-fA-F0-9]+)"',
        resources,
    )
    if match is None or match.group(1) != crashlytics_id:
        raise ValueError('The packaged APK does not contain the expected Crashlytics mapping ID')
    badging = subprocess.check_output([str(aapt2), 'dump', 'badging', str(apk)], text=True)
    package = dict(re.findall(r"(name|versionCode|versionName)='([^']*)'", badging.splitlines()[0]))
    if package != {'name': 'com.eyther.lumbridge', 'versionCode': str(version_code), 'versionName': version_name}:
        raise ValueError('The packaged APK does not match the planned Loom release version')
    if not re.fullmatch(r'[a-fA-F0-9]{40}', source_commit):
        raise ValueError('The source commit must identify the exact release')
    return {
        'package': package['name'],
        'versionName': version_name,
        'versionCode': int(version_code),
        'sourceCommit': source_commit,
        'r8MapId': r8_id,
        'crashlyticsMappingId': crashlytics_id,
        'mappingSha256': sha256(mapping),
        'apkSha256': sha256(apk),
        'compiler': header.get('compiler'),
        'compilerVersion': header.get('compiler_version'),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('mapping', 'generated-id', 'resource', 'apk', 'aapt2', 'output'):
        parser.add_argument('--' + name, required=True, type=Path)
    for name in ('version-name', 'version-code', 'source-commit'):
        parser.add_argument('--' + name, required=True)
    args = vars(parser.parse_args())
    output = args.pop('output')
    metadata = verify(**args)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(metadata, indent=2) + '\n')
    print(f"Verified symbols for Loom {metadata['versionName']} ({metadata['versionCode']}), R8 {metadata['r8MapId']}")


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, ET.ParseError, subprocess.CalledProcessError, zipfile.BadZipFile) as error:
        raise SystemExit(f'Android symbol verification failed: {error}')
