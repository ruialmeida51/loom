import { createSign } from 'node:crypto';
import { readFileSync, appendFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

const OAUTH_URL = 'https://oauth2.googleapis.com/token';
const API_ROOT = 'https://androidpublisher.googleapis.com/androidpublisher/v3';
const PACKAGE_NAME = 'com.eyther.lumbridge';

export function accessAssertion(account, now = Math.floor(Date.now() / 1000)) {
  const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url');
  const header = encode({ alg: 'RS256', typ: 'JWT' });
  const payload = encode({
    iss: account.client_email,
    scope: 'https://www.googleapis.com/auth/androidpublisher',
    aud: OAUTH_URL,
    iat: now,
    exp: now + 3600,
  });
  const message = `${header}.${payload}`;
  const signature = createSign('RSA-SHA256').update(message).sign(account.private_key, 'base64url');
  return `${message}.${signature}`;
}

export function nextVersionCode(tracks, bundles, apks, requested = '') {
  const releases = (tracks.tracks ?? []).flatMap(track => track.releases ?? []);
  const codes = releases.flatMap(release => release.versionCodes ?? []);
  codes.push(...(bundles.bundles ?? []).map(bundle => bundle.versionCode));
  codes.push(...(apks.apks ?? []).map(apk => apk.versionCode));
  const maximum = Math.max(0, ...codes.map(Number).filter(Number.isSafeInteger));
  const next = requested ? Number(requested) : maximum + 1;
  if (!Number.isSafeInteger(next) || next <= maximum || next > 2_100_000_000 || next < 1) {
    throw new Error(`Choose a version code higher than ${maximum} and below 2100000001.`);
  }
  return next;
}

export async function plan(account, requested = '', request = fetch) {
  const tokenResponse = await request(OAUTH_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: accessAssertion(account),
    }),
  });
  if (!tokenResponse.ok) throw new Error('Google refused service-account authentication. Check the service-account JSON key.');
  const { access_token: token } = await tokenResponse.json();
  if (!token) throw new Error('Google did not return an access token.');
  const headers = { authorization: `Bearer ${token}`, 'content-type': 'application/json' };
  const app = `${API_ROOT}/applications/${PACKAGE_NAME}/edits`;
  const editResponse = await request(app, { method: 'POST', headers, body: '{}' });
  if (!editResponse.ok) throw new Error('Cannot access Loom in Play Console. Enable the Android Developer API and grant the service account app access and testing-release permissions.');
  const edit = await editResponse.json();
  if (!edit.id || !/^[A-Za-z0-9_-]+$/.test(edit.id)) throw new Error('Play returned an invalid edit identifier.');
  const endpoint = `${app}/${encodeURIComponent(edit.id)}`;
  let result;
  try {
    const responses = await Promise.all(['tracks', 'bundles', 'apks'].map(resource => request(`${endpoint}/${resource}`, { headers })));
    if (responses.some(response => !response.ok)) throw new Error('Could not read existing Play version codes. Check service-account permissions.');
    const [tracks, bundles, apks] = await Promise.all(responses.map(response => response.json()));
    result = nextVersionCode(tracks, bundles, apks, requested);
  } finally {
    const response = await request(endpoint, { method: 'DELETE', headers });
    if (!response.ok && response.status !== 404) throw new Error('Could not close the temporary Play version-planning edit. No release was committed.');
  }
  return result;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const [credentialPath, requested = ''] = process.argv.slice(2);
    const account = JSON.parse(readFileSync(credentialPath, 'utf8'));
    const code = await plan(account, requested);
    if (process.env.GITHUB_OUTPUT) appendFileSync(process.env.GITHUB_OUTPUT, `version_code=${code}\n`);
    process.stdout.write(`Next Loom Play version code: ${code}\n`);
  } catch (error) {
    const safeMessages = ['Google refused', 'Google did not', 'Cannot access Loom', 'Play returned', 'Could not', 'Choose a version'];
    process.stderr.write(safeMessages.some(prefix => error.message.startsWith(prefix)) ? `${error.message}\n` : 'Play release planning failed. Verify the service-account key and its app permissions.\n');
    process.exitCode = 1;
  }
}
