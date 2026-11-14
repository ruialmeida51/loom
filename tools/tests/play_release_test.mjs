import test from 'node:test';
import assert from 'node:assert/strict';
import { generateKeyPairSync, verify } from 'node:crypto';
import { accessAssertion, nextVersionCode, plan } from '../plan_play_release.mjs';

const keys = generateKeyPairSync('rsa', { modulusLength: 2048 });
const account = { client_email: 'test@loom-test.iam.gserviceaccount.com', private_key: keys.privateKey.export({ type: 'pkcs8', format: 'pem' }) };

test('version allocation considers all tracks, bundles and APKs', () => {
  const tracks = { tracks: [{ releases: [{ versionCodes: ['2', '8'] }] }] };
  assert.equal(nextVersionCode(tracks, { bundles: [{ versionCode: 11 }] }, { apks: [{ versionCode: 9 }] }), 12);
  assert.equal(nextVersionCode(tracks, {}, {}, '15'), 15);
  assert.throws(() => nextVersionCode(tracks, {}, {}, '8'));
  assert.throws(() => nextVersionCode({}, {}, {}, 'invalid'));
});

test('OAuth assertions target only the Google token endpoint and publisher scope', () => {
  const assertion = accessAssertion(account, 1000);
  const [header, body, signature] = assertion.split('.');
  const claims = JSON.parse(Buffer.from(body, 'base64url'));
  assert.equal(claims.aud, 'https://oauth2.googleapis.com/token');
  assert.equal(claims.scope, 'https://www.googleapis.com/auth/androidpublisher');
  assert.equal(claims.exp, 4600);
  assert.ok(verify('RSA-SHA256', Buffer.from(`${header}.${body}`), keys.publicKey, Buffer.from(signature, 'base64url')));
});

test('planning closes the temporary edit and never commits a release', async () => {
  const requests = [];
  const request = async (url, options = {}) => {
    requests.push([url, options.method ?? 'GET']);
    if (url.includes('oauth2')) return Response.json({ access_token: 'test-access-token' });
    if (options.method === 'POST') return Response.json({ id: 'test-edit' });
    if (url.endsWith('/bundles')) return Response.json({ bundles: [{ versionCode: 3 }] });
    if (url.endsWith('/tracks')) return Response.json({ tracks: [] });
    if (url.endsWith('/apks')) return Response.json({ apks: [] });
    if (options.method === 'DELETE') return new Response(null, { status: 204 });
    throw new Error('Unexpected request');
  };
  assert.equal(await plan(account, '', request), 4);
  assert.equal(requests.at(-1)[1], 'DELETE');
  assert.ok(requests.every(([url]) => !url.includes(':commit')));
});

test('a failed version read still cleans up its temporary edit', async () => {
  let deleted = false;
  const request = async (url, options = {}) => {
    if (url.includes('oauth2')) return Response.json({ access_token: 'test-access-token' });
    if (options.method === 'POST') return Response.json({ id: 'test-edit' });
    if (options.method === 'DELETE') { deleted = true; return new Response(null, { status: 204 }); }
    return new Response(null, { status: 403 });
  };
  await assert.rejects(plan(account, '', request), /existing Play version/);
  assert.ok(deleted);
});
