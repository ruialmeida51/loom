#!/usr/bin/env python3
"""Gate Play uploads on complete, public policy pages matching this release."""
import argparse
from html.parser import HTMLParser
import json
import urllib.request
from build_legal_site import configuration


class PolicyMetadata(HTMLParser):
    def __init__(self):
        super().__init__()
        self.version = None

    def handle_starttag(self, tag, attributes):
        values = dict(attributes)
        if tag == 'meta' and values.get('name') == 'loom-policy-version':
            self.version = values.get('content')


def check(live=False):
    values = configuration()
    if '@' not in values['SUPPORT_EMAIL']:
        raise ValueError('A working support/privacy email is required')
    if not live:
        return
    site = values['LEGAL_SITE_URL'].rstrip('/')
    with urllib.request.urlopen(site + '/release.json', timeout=30) as response:
        deployed = json.load(response)
    if deployed.get('app') != 'Loom' or deployed.get('package') != 'com.eyther.lumbridge':
        raise ValueError('The published legal site does not identify this Loom app')
    if deployed.get('draft') or deployed.get('version') != values['POLICY_VERSION']:
        raise ValueError('Publish the current, final policy version before uploading to Play')
    if deployed.get('publisher') != values['PUBLISHER_NAME']:
        raise ValueError('Published publisher details differ from this release')
    for slug in ('privacy','terms','delete-account'):
        with urllib.request.urlopen(f'{site}/{slug}/', timeout=30) as response:
            body = response.read().decode()
        metadata = PolicyMetadata()
        metadata.feed(body)
        if values['SUPPORT_EMAIL'] not in body or metadata.version != values['POLICY_VERSION']:
            raise ValueError(f'The public {slug} page is incomplete')
    print('Public privacy, terms and deletion pages verified')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--live',action='store_true')
    arguments = parser.parse_args()
    try:
        check(arguments.live)
    except (ValueError, OSError) as error:
        raise SystemExit(str(error))
