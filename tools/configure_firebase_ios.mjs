import { readFileSync, writeFileSync, appendFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import { authorizeGoogle, CLOUD_SCOPE } from './google_service_account.mjs';

const PROJECT = 'lumbridge-app';
const BUNDLE = 'com.eyther.lumbridge';
const API = 'https://firebase.googleapis.com/v1beta1/';

/** Idempotently register Loom's Apple client; never modify or reuse another app's configuration. */
export async function configureFirebaseIos(token, request = fetch, sleep = ms => new Promise(resolve => setTimeout(resolve, ms)), allowCreate = true) {
  async function call(path, options = {}) {
    const response = await request(`${API}${path}`, {
      ...options,
      headers: { authorization: `Bearer ${token}`, 'content-type': 'application/json' },
      signal: AbortSignal.timeout(30_000),
    });
    if (!response.ok) {
      if (response.status === 403) throw new Error('Firebase denied iOS setup. The existing service account needs firebase.clients.list, firebase.clients.get and firebase.clients.create on lumbridge-app (Firebase Develop Admin includes them).');
      throw new Error(`Firebase iOS setup failed (HTTP ${response.status}).`);
    }
    return response.json();
  }
  let pageToken = '';
  let app;
  do {
    const query = new URLSearchParams({ pageSize: '100', ...(pageToken ? { pageToken } : {}) });
    const page = await call(`projects/${PROJECT}/iosApps?${query}`);
    app = page.apps?.find(candidate => candidate.bundleId === BUNDLE);
    pageToken = page.nextPageToken ?? '';
  } while (!app && pageToken);
  if (!app) {
    if (!allowCreate) throw new Error('Firebase iOS client is missing. Run Configure Loom iOS Firebase before this build.');
    let operation = await call(`projects/${PROJECT}/iosApps`, {
      method: 'POST', body: JSON.stringify({ displayName: 'Loom iOS', bundleId: BUNDLE }),
    });
    if (!/^operations\/[A-Za-z0-9_:/=+.-]+$/.test(operation.name ?? '') || operation.name.includes('..')) throw new Error('Firebase returned an invalid registration operation.');
    const deadline = Date.now() + 120_000;
    while (!operation.done && Date.now() < deadline) {
      await sleep(1000);
      operation = await call(operation.name);
    }
    if (!operation.done || operation.error) throw new Error('Firebase could not finish iOS registration.');
    app = operation.response;
  }
  if (app?.bundleId !== BUNDLE || !/^projects\/lumbridge-app\/iosApps\/1:\d+:ios:[a-f0-9]+$/.test(app.name ?? '')) throw new Error('Firebase returned a different iOS app.');
  const config = await call(`${app.name}/config`);
  if (!config.configFileContents) throw new Error('Firebase did not return the iOS configuration.');
  const plist = Buffer.from(config.configFileContents, 'base64').toString('utf8');
  if (!plist.includes(BUNDLE) || !plist.includes(PROJECT) || !plist.includes(app.appId)) throw new Error('Firebase returned a configuration for a different app.');
  return { appId: app.appId, plist };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const [credentialPath, outputPath, mode] = process.argv.slice(2);
    const account = JSON.parse(readFileSync(credentialPath, 'utf8'));
    const token = await authorizeGoogle(account, CLOUD_SCOPE);
    const result = await configureFirebaseIos(token, fetch, undefined, mode !== '--existing-only');
    writeFileSync(outputPath, result.plist, { mode: 0o600 });
    if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY, `Registered or found Loom iOS in Firebase project ${PROJECT}: ${result.appId}.\n`);
    process.stdout.write(`Firebase iOS configuration prepared for ${BUNDLE}.\n`);
  } catch (error) {
    process.stderr.write(error.message?.startsWith('Firebase ') ? `${error.message}\n` : 'Firebase iOS setup failed. Check the existing service account.\n');
    process.exitCode = 1;
  }
}
