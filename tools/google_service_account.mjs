import { createSign } from 'node:crypto';

export const PUBLISHER_SCOPE = 'https://www.googleapis.com/auth/androidpublisher';
export const CLOUD_SCOPE = 'https://www.googleapis.com/auth/cloud-platform';
const OAUTH_URL = 'https://oauth2.googleapis.com/token';

export function accessAssertion(account, now = Math.floor(Date.now() / 1000), scope = PUBLISHER_SCOPE) {
  const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url');
  const message = `${encode({ alg: 'RS256', typ: 'JWT' })}.${encode({
    iss: account.client_email, scope, aud: OAUTH_URL, iat: now, exp: now + 3600,
  })}`;
  return `${message}.${createSign('RSA-SHA256').update(message).sign(account.private_key, 'base64url')}`;
}

export async function authorizeGoogle(account, scope = PUBLISHER_SCOPE, request = fetch) {
  const response = await request(OAUTH_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: accessAssertion(account, Math.floor(Date.now() / 1000), scope),
    }),
  });
  if (!response.ok) throw new Error('Google refused service-account authentication. Check the service-account JSON key.');
  const { access_token: token } = await response.json();
  if (!token) throw new Error('Google did not return an access token.');
  return token;
}
