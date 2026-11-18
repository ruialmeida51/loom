import test from 'node:test';
import assert from 'node:assert/strict';
import { configureFirebaseIos } from '../configure_firebase_ios.mjs';

const app = { bundleId: 'com.eyther.lumbridge', appId: '1:123:ios:abcdef', name: 'projects/lumbridge-app/iosApps/1:123:ios:abcdef' };
const plist = `<plist>com.eyther.lumbridge lumbridge-app ${app.appId}</plist>`;
const ok = data => ({ ok: true, json: async () => data });

test('existing matching iOS client is reused without a write', async () => {
  const methods = [];
  const result = await configureFirebaseIos('test', async (url, options) => {
    methods.push(options.method ?? 'GET');
    return ok(url.endsWith('/config') ? { configFileContents: Buffer.from(plist).toString('base64') } : { apps: [app] });
  });
  assert.equal(result.plist, plist);
  assert.deepEqual(methods, ['GET', 'GET']);
});

test('new registration waits for completion and verifies the returned bundle', async () => {
  const replies = [
    { apps: [] }, { name: 'operations/project:123/ios:abcdef==' },
    { name: 'operations/project:123/ios:abcdef==', done: true, response: app },
    { configFileContents: Buffer.from(plist).toString('base64') },
  ];
  const writes = [];
  const result = await configureFirebaseIos('test', async (url, options) => {
    if (options.method === 'POST') writes.push(JSON.parse(options.body));
    return ok(replies.shift());
  }, async () => {});
  assert.equal(result.appId, app.appId);
  assert.deepEqual(writes, [{ displayName: 'Loom iOS', bundleId: 'com.eyther.lumbridge' }]);
});

test('wrong configuration and missing permissions fail without printing credentials', async () => {
  await assert.rejects(configureFirebaseIos('private-token', async () => ({ ok: false, status: 403 })), /firebase.clients.create/);
  await assert.rejects(configureFirebaseIos('private-token', async url => ok(url.endsWith('/config') ? { configFileContents: Buffer.from('another app').toString('base64') } : { apps: [app] })), /different app/);
});

test('ordinary iOS builds never register a missing client', async () => {
  const methods = [];
  await assert.rejects(configureFirebaseIos('test', async (url, options) => {
    methods.push(options.method ?? 'GET');
    return ok({ apps: [] });
  }, undefined, false), /client is missing/);
  assert.deepEqual(methods, ['GET']);
});
