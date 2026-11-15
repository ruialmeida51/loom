import test from 'node:test';
import assert from 'node:assert/strict';
import { nextReleaseVersion, nextVersionName } from '../plan_release.mjs';
import { FirebaseDistribution, parseTesterGroups } from '../firebase_distribution.mjs';
import { ReleaseHistory } from '../release_history.mjs';

const app = 'projects/123/apps/1:123:android:abc123';
const release = { name: `${app}/releases/build-50`, displayVersion: '4.0.0', buildVersion: '50', firebaseConsoleUri: 'https://console.firebase.google.com/project/loom/appdistribution' };
const previous = { minimum: '4.0.0', play: { versionCode: 50, versionNames: ['0.2.0'] }, firebase: [], history: [] };

test('the next release starts at 4.0.0 with a code above the existing Play upload', () => {
  assert.deepEqual(nextReleaseVersion(previous), { versionName: '4.0.0', versionCode: 50 });
});

test('Firebase, Play and durable history all advance the same patch and code', () => {
  assert.deepEqual(nextReleaseVersion({ ...previous, firebase: [{ displayVersion: '4.0.2', buildVersion: '52' }] }), { versionName: '4.0.3', versionCode: 53 });
  assert.deepEqual(nextReleaseVersion({ ...previous, history: [{ destination: 'firebase', versionName: '4.0.3', versionCode: 53 }] }), { versionName: '4.0.4', versionCode: 54 });
});

test('semantic versions compare numerically and an explicit major/minor floor starts a new series', () => {
  assert.equal(nextVersionName('4.0.0', ['4.0.9', '4.0.10', 'not-a-version']), '4.0.11');
  assert.equal(nextVersionName('4.0.0', ['4.1.2', '4.0.99']), '4.1.3');
  assert.equal(nextVersionName('5.0.0', ['4.9.99']), '5.0.0');
  assert.throws(() => nextVersionName('4.0', []));
});

test('manual Android codes cannot reuse a Firebase build or exceed Play limits', () => {
  const input = { ...previous, firebase: [{ displayVersion: '4.0.0', buildVersion: '55' }] };
  for (const requestedCode of ['55', '-1', '1.5', 'nope', '2100000001', '1e3']) {
    assert.throws(() => nextReleaseVersion({ ...input, requestedCode }));
  }
  assert.equal(nextReleaseVersion({ ...input, requestedCode: '70' }).versionCode, 70);
});

test('Firebase version reads include every result page', async () => {
  const urls = [];
  const client = new FirebaseDistribution(app, 'fixture-token', async url => {
    urls.push(url);
    return Response.json(url.includes('pageToken=') ? { releases: [{ buildVersion: '49' }] } : { releases: [release], nextPageToken: 'next page' });
  });
  assert.equal((await client.releases()).length, 2);
  assert.ok(urls[1].includes('pageToken=next+page'));
});

function uploadFixture({ groups = [], failed = false } = {}) {
  const calls = [];
  const request = async (url, options = {}) => {
    calls.push({ url, options });
    if (url.includes('/upload/')) return Response.json({ name: `${app}/releases/-/operations/upload-50` });
    if (url.includes('/operations/')) return Response.json(failed ? { done: true, error: { code: 3 } } : { done: true, response: { release } });
    return Response.json({});
  };
  const client = new FirebaseDistribution(app, 'fixture-token', request);
  return { calls, upload: () => client.upload({ binary: Buffer.from('fixture-apk'), fileName: 'Loom 4.apk', notes: 'Release notes', groups }, async () => {}) };
}

test('Firebase uploads poll processing, attach notes and send no unsolicited invitations', async () => {
  const fixture = uploadFixture();
  assert.equal((await fixture.upload()).displayVersion, '4.0.0');
  assert.equal(fixture.calls[0].options.headers['X-Goog-Upload-Protocol'], 'raw');
  assert.equal(fixture.calls[0].options.headers['X-Goog-Upload-File-Name'], 'Loom%204.apk');
  assert.deepEqual(JSON.parse(fixture.calls.at(-1).options.body), { releaseNotes: { text: 'Release notes' } });
  assert.ok(fixture.calls.every(call => !call.url.endsWith(':distribute')));
});

test('only explicitly configured Firebase groups are distributed to', async () => {
  const fixture = uploadFixture({ groups: ['internal'] });
  await fixture.upload();
  assert.ok(fixture.calls.at(-1).url.endsWith(':distribute'));
  assert.deepEqual(JSON.parse(fixture.calls.at(-1).options.body), { groupAliases: ['internal'] });
});

test('a failed APK process never sends a distribution request', async () => {
  const fixture = uploadFixture({ groups: ['internal'], failed: true });
  await assert.rejects(fixture.upload(), /could not finish processing/);
  assert.equal(fixture.calls.length, 2);
});

test('Firebase permission failures give the exact missing role without printing server content', async () => {
  const client = new FirebaseDistribution(app, 'fixture-token', async () => new Response('opaque-private-error', { status: 403 }));
  await assert.rejects(client.releases(), error => /Firebase App Distribution Admin/.test(error.message) && !error.message.includes('opaque-private-error'));
});

test('an upload operation cannot redirect credentials to another service', async () => {
  let calls = 0;
  const client = new FirebaseDistribution(app, 'fixture-token', async () => { calls++; return Response.json({ name: 'https://another.test/steal' }); });
  await assert.rejects(client.upload({ binary: Buffer.from('apk'), fileName: 'app.apk', notes: '' }, async () => {}), /invalid upload operation/);
  assert.equal(calls, 1);
});

test('version history ignores failed attempts and automatic workflow deployments', async () => {
  const history = new ReleaseHistory('owner/loom', 'fixture-github-token', async url => {
    if (url.includes('environment=play-internal')) return Response.json([]);
    if (url.includes('/3/statuses')) return Response.json([{ state: 'failure' }]);
    if (url.includes('/2/statuses')) return Response.json([{ state: 'success' }]);
    return Response.json([
      { id: 4, payload: {} },
      { id: 3, payload: { loomRelease: { destination: 'firebase', versionName: '4.0.2', versionCode: 52 } } },
      { id: 2, payload: { loomRelease: { destination: 'firebase', versionName: '4.0.1', versionCode: 51 } } },
    ]);
  });
  assert.deepEqual(await history.versions(), [{ destination: 'firebase', versionName: '4.0.1', versionCode: 51 }]);
});

test('recording Firebase history creates neither a GitHub release nor a Git tag', async () => {
  const calls = [];
  const history = new ReleaseHistory('owner/loom', 'fixture-github-token', async (url, options) => {
    calls.push({ url, body: JSON.parse(options.body) });
    return Response.json({ id: 50 });
  });
  await history.record({ destination: 'firebase', versionName: '4.0.0', versionCode: 50, sha: 'a'.repeat(40), url: release.firebaseConsoleUri, notes: ['Home quick actions.'] });
  assert.ok(calls.every(call => call.url.includes('/deployments')));
  assert.deepEqual(calls[0].body.payload.loomRelease, { destination: 'firebase', versionName: '4.0.0', versionCode: 50, notes: ['Home quick actions.'] });
  assert.equal(calls[0].body.ref, 'a'.repeat(40));
  assert.equal(calls[1].body.state, 'success');
});


test('comment-only Firebase group configuration stays upload-only even with commas', () => {
  assert.deepEqual(parseTesterGroups('# Existing Firebase tester group aliases, one per line. Empty means upload only.\n\n'), []);
  assert.deepEqual(parseTesterGroups('  # A comment, another comma, and spaces\r\n'), []);
});

test('Firebase group configuration accepts line and comma separators and removes duplicates', () => {
  assert.deepEqual(parseTesterGroups('internal, qa\n# ignored, whole line\ncouples\r\nqa\n'), ['internal', 'qa', 'couples']);
});

test('malformed group aliases fail instead of silently changing recipients', () => {
  for (const input of ['internal testers', 'developer@example.test', 'projects/app/groups/internal', 'internal # comment']) {
    assert.throws(() => parseTesterGroups(input), /Firebase tester group aliases/);
  }
});
