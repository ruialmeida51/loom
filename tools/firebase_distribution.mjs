import { authorizeGoogle, CLOUD_SCOPE } from './google_service_account.mjs';

const API = 'https://firebaseappdistribution.googleapis.com';
const delay = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

/** Strip comment lines before parsing comma-separated aliases; comments may contain commas. */
export function parseTesterGroups(configuration) {
  const groups = configuration.split(/\r?\n/)
    .map(line => line.trim())
    .filter(line => line && !line.startsWith('#'))
    .flatMap(line => line.split(','))
    .map(group => group.trim())
    .filter(Boolean);
  if (groups.some(group => !/^[A-Za-z0-9_-]+$/.test(group))) {
    throw new Error('Firebase tester group aliases may contain only letters, digits, underscores and hyphens.');
  }
  return [...new Set(groups)];
}

/** Uses the same published REST contract as Firebase's CLI; no CLI token or developer login is needed. */
export class FirebaseDistribution {
  constructor(app, token, request = fetch) {
    if (!/^projects\/\d+\/apps\/1:\d+:android:[a-f0-9]+$/.test(app)) throw new Error('Invalid Firebase Android app identifier.');
    this.app = app;
    this.token = token;
    this.request = request;
  }

  static async connect(account, app, request = fetch) {
    return new FirebaseDistribution(app, await authorizeGoogle(account, CLOUD_SCOPE, request), request);
  }

  async call(path, options = {}) {
    const response = await this.request(`${API}/${path}`, {
      ...options,
      headers: { authorization: `Bearer ${this.token}`, 'content-type': 'application/json', ...options.headers },
      signal: AbortSignal.timeout(options.body instanceof Uint8Array ? 180_000 : 30_000),
    });
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        throw new Error('Firebase denied App Distribution access. Enable the Firebase App Distribution API and grant this service account Firebase App Distribution Admin on lumbridge-app.');
      }
      throw new Error(`Firebase App Distribution request failed (HTTP ${response.status}).`);
    }
    return response.status === 204 ? {} : response.json();
  }

  async releases() {
    const releases = [];
    let pageToken = '';
    do {
      const query = new URLSearchParams({ pageSize: '100', ...(pageToken ? { pageToken } : {}) });
      const page = await this.call(`v1/${this.app}/releases?${query}`);
      releases.push(...(page.releases ?? []));
      pageToken = page.nextPageToken ?? '';
    } while (pageToken);
    return releases;
  }

  async upload({ binary, fileName, notes, groups = [] }, sleep = delay) {
    const operation = await this.call(`upload/v1/${this.app}/releases:upload`, {
      method: 'POST', body: binary,
      headers: { 'content-type': 'application/octet-stream', 'X-Goog-Upload-Protocol': 'raw', 'X-Goog-Upload-File-Name': encodeURIComponent(fileName) },
    });
    const prefix = `${this.app}/releases/`;
    if (!operation.name?.startsWith(prefix) || !/^[A-Za-z0-9_:/-]+$/.test(operation.name) || operation.name.includes('..')) {
      throw new Error('Firebase returned an invalid upload operation.');
    }
    let result = operation;
    const deadline = Date.now() + 5 * 60_000;
    while (!result.done && Date.now() < deadline) {
      await sleep(3000);
      result = await this.call(`v1/${operation.name}`);
    }
    if (!result.done || result.error) throw new Error('Firebase could not finish processing the APK. Check the App Distribution console and upload permissions.');
    const release = result.response?.release;
    if (!release?.name?.startsWith(prefix) || !/^[A-Za-z0-9_:/-]+$/.test(release.name) || release.name.includes('..')) {
      throw new Error('Firebase returned an invalid release.');
    }
    await this.call(`v1/${release.name}?updateMask=releaseNotes.text`, {
      method: 'PATCH', body: JSON.stringify({ releaseNotes: { text: notes } }),
    });
    // An upload alone never guesses recipients or sends tester invitations.
    if (groups.length) {
      await this.call(`v1/${release.name}:distribute`, { method: 'POST', body: JSON.stringify({ groupAliases: groups }) });
    }
    return release;
  }
}
