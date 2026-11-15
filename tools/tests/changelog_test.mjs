import test from 'node:test';
import assert from 'node:assert/strict';
import { archiveChangelog, prepareChangelog, recordChangelog, renderReleaseNotes } from '../changelog.mjs';
import { nextReleaseVersion } from '../plan_release.mjs';

const source = '# Changelog\n\n## [Unreleased]\n\n- Add Home shortcuts.\n- Separate trip balances.\n\n## [4.0.0] - 2026-11-14\n\n- Initial release.\n';
const version = { versionName: '4.0.1', versionCode: 51 };
const snapshot = () => prepareChangelog(source, version, '2026-11-15');
const response = (status, body = {}) => ({ status, ok: status >= 200 && status < 300, json: async () => body });
const file = (content, sha = 'original-file') => ({ type: 'file', encoding: 'base64', content: Buffer.from(content).toString('base64'), sha });

test('freezes user-facing notes for the version allocated across both destinations', () => {
  const planned = nextReleaseVersion({ minimum: '4.0.0', play: { versionCode: 51, versionNames: ['4.0.0'] }, firebase: [], history: [] });
  const release = prepareChangelog(source, planned, '2026-11-15');
  assert.deepEqual(release, { ...version, date: '2026-11-15', entries: ['Add Home shortcuts.', 'Separate trip balances.'] });
  assert.equal(renderReleaseNotes(release), '- Add Home shortcuts.\n- Separate trip balances.\n');
});

test('archiving preserves notes merged while the upload was running', () => {
  const newerMain = source.replace('- Add Home shortcuts.', '- A later fix.\n- Add Home shortcuts.');
  const archived = archiveChangelog(newerMain, snapshot());
  assert.match(archived, /## \[Unreleased\]\n\n- A later fix\.\n\n## \[4\.0\.1\] - 2026-11-15/);
  assert.match(archived, /## \[4\.0\.0\] - 2026-11-14\n\n- Initial release\./);
});

test('retrying the archive is idempotent and never changes an existing release', () => {
  const archived = archiveChangelog(source, snapshot());
  assert.equal(archiveChangelog(archived, snapshot()), archived);
  assert.throws(() => archiveChangelog(archived, { ...snapshot(), entries: ['Different notes.'] }), /different notes/);
});

test('a promotion or rebuild with no new notes reuses the latest released notes', () => {
  const archived = archiveChangelog(source, snapshot());
  const promotion = prepareChangelog(archived, { versionName: '4.0.2', versionCode: 52 }, '2026-11-16');
  assert.deepEqual(promotion.entries, snapshot().entries);
  const result = archiveChangelog(archived, promotion);
  assert.match(result, /## \[4\.0\.2\] - 2026-11-16/);
  assert.match(result, /## \[4\.0\.1\] - 2026-11-15/);
});

test('Play limits include bullet formatting and the trailing newline', () => {
  const boundary = { ...snapshot(), entries: ['x'.repeat(497)] };
  assert.equal(renderReleaseNotes(boundary).length, 500);
  assert.throws(() => renderReleaseNotes({ ...boundary, entries: ['x'.repeat(498)] }), /500 characters/);
  assert.throws(() => renderReleaseNotes({ ...boundary, entries: 'not an array' }), /single-line entries/);
});

test('malformed changelogs fail before any upload can use their notes', () => {
  assert.throws(() => prepareChangelog('# Changelog\n', version, '2026-11-15'), /Unreleased/);
  assert.throws(() => prepareChangelog('## [Unreleased]\nA paragraph instead of a bullet.\n', version, '2026-11-15'), /one bullet per line/);
  assert.throws(() => prepareChangelog('## [Unreleased]\n', version, '2026-11-15'), /nonempty single-line entries/);
});

test('a concurrent GitHub update retries from the new file and preserves its changes', async () => {
  let reads = 0;
  const writes = [];
  const request = async (url, options) => {
    if (options.method !== 'PUT') {
      reads++;
      assert.match(url, /contents\/CHANGELOG\.md\?ref=main$/);
      return response(200, file(reads === 1 ? source : source.replace('- Add Home shortcuts.', '- Newer work.\n- Add Home shortcuts.'), `sha-${reads}`));
    }
    const body = JSON.parse(options.body);
    assert.equal(body.branch, 'main');
    writes.push(body);
    return response(writes.length === 1 ? 409 : 200);
  };
  await recordChangelog({ repository: 'owner/loom', token: 'test-token', release: snapshot(), request });
  assert.equal(reads, 2);
  assert.equal(writes[1].sha, 'sha-2');
  assert.match(Buffer.from(writes[1].content, 'base64').toString('utf8'), /## \[Unreleased\]\n\n- Newer work\./);
});

test('an already archived release does not write another bot commit', async () => {
  let calls = 0;
  await recordChangelog({ repository: 'owner/loom', token: 'test-token', release: snapshot(), request: async (_, options) => {
    calls++;
    assert.notEqual(options.method, 'PUT');
    return response(200, file(archiveChangelog(source, snapshot())));
  } });
  assert.equal(calls, 1);
});

test('a denied repository write fails once and leaves successful-upload history separate', async () => {
  let writes = 0;
  await assert.rejects(recordChangelog({ repository: 'owner/loom', token: 'test-token', release: snapshot(), request: async (_, options) => {
    if (options.method !== 'PUT') return response(200, file(source));
    writes++;
    return response(403);
  } }), /update failed \(HTTP 403\)/);
  assert.equal(writes, 1);
});
