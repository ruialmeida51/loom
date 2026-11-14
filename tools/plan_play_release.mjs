import { authorizeGoogle, accessAssertion, PUBLISHER_SCOPE } from './google_service_account.mjs';
import { readFileSync, appendFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

export { accessAssertion };

const API_ROOT = 'https://androidpublisher.googleapis.com/androidpublisher/v3';
const PACKAGE_NAME = 'com.eyther.lumbridge';

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
  const token = await authorizeGoogle(account, PUBLISHER_SCOPE, request);
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
    result = {
      versionCode: nextVersionCode(tracks, bundles, apks, requested),
      versionNames: (tracks.tracks ?? []).flatMap(track => track.releases ?? []).map(release => release.name).filter(Boolean),
    };
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
    if (process.env.GITHUB_OUTPUT) appendFileSync(process.env.GITHUB_OUTPUT, `version_code=${code.versionCode}\n`);
    process.stdout.write(`Next Loom Play version code: ${code.versionCode}\n`);
  } catch (error) {
    const safeMessages = ['Google refused', 'Google did not', 'Cannot access Loom', 'Play returned', 'Could not', 'Choose a version'];
    process.stderr.write(safeMessages.some(prefix => error.message.startsWith(prefix)) ? `${error.message}\n` : 'Play release planning failed. Verify the service-account key and its app permissions.\n');
    process.exitCode = 1;
  }
}
