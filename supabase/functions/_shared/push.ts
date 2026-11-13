export interface Delivery {
  delivery_id: string;
  device_id: string;
  token: string;
  platform: "fcm" | "apns";
  notification_id: string;
  title: string;
  body: string;
  data: Record<string, string>;
}
export type DeliveryResult = "sent" | "retry" | "dead";
const encoder = new TextEncoder();
function b64(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes)).replace(/=/g, "").replace(
    /\+/g,
    "-",
  ).replace(/\//g, "_");
}
function segment(value: unknown): string {
  return b64(encoder.encode(JSON.stringify(value)));
}
function pem(value: string): Uint8Array {
  return Uint8Array.from(
    atob(value.replace(/-----[^-]+-----/g, "").replace(/\s/g, "")),
    (character) => character.charCodeAt(0),
  );
}
async function jwt(
  header: unknown,
  payload: unknown,
  privateKey: string,
  algorithm: "RS256" | "ES256",
): Promise<string> {
  const data = `${segment(header)}.${segment(payload)}`;
  const keyAlgorithm = algorithm === "RS256"
    ? { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }
    : { name: "ECDSA", namedCurve: "P-256" };
  const key = await crypto.subtle.importKey(
    "pkcs8",
    pem(privateKey).buffer as ArrayBuffer,
    keyAlgorithm,
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign(
    algorithm === "RS256"
      ? { name: "RSASSA-PKCS1-v1_5" }
      : { name: "ECDSA", hash: "SHA-256" },
    key,
    encoder.encode(data),
  );
  return `${data}.${b64(new Uint8Array(signature))}`;
}
function required(name: string): string {
  const value = Deno.env.get(name);
  if (!value) throw new Error("Push credentials are not configured");
  return value.replace(/\\n/g, "\n");
}
let googleToken: { promise: Promise<string>; expiresAt: number } | undefined;
async function fcmAccessToken(): Promise<string> {
  if (googleToken && googleToken.expiresAt > Date.now()) {
    return googleToken.promise;
  }
  const promise = (async () => {
    const now = Math.floor(Date.now() / 1000);
    const assertion = await jwt(
      { alg: "RS256", typ: "JWT" },
      {
        iss: required("FCM_CLIENT_EMAIL"),
        scope: "https://www.googleapis.com/auth/firebase.messaging",
        aud: "https://oauth2.googleapis.com/token",
        iat: now,
        exp: now + 3600,
      },
      required("FCM_PRIVATE_KEY"),
      "RS256",
    );
    const result = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      body: new URLSearchParams({
        grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
        assertion,
      }),
      signal: AbortSignal.timeout(10_000),
    });
    if (!result.ok) throw new Error("FCM authorization failed");
    const body = await result.json();
    if (typeof body.access_token !== "string") {
      throw new Error("FCM authorization failed");
    }
    return body.access_token;
  })();
  googleToken = { promise, expiresAt: Date.now() + 45 * 60_000 };
  try {
    return await promise;
  } catch (error) {
    googleToken = undefined;
    throw error;
  }
}
let appleToken: { token: string; expiresAt: number } | undefined;
async function apnsAccessToken(): Promise<string> {
  if (appleToken && appleToken.expiresAt > Date.now()) return appleToken.token;
  const token = await jwt(
    { alg: "ES256", kid: required("APNS_KEY_ID") },
    { iss: required("APNS_TEAM_ID"), iat: Math.floor(Date.now() / 1000) },
    required("APNS_PRIVATE_KEY"),
    "ES256",
  );
  appleToken = { token, expiresAt: Date.now() + 45 * 60_000 };
  return token;
}
export async function deliver(notification: Delivery): Promise<DeliveryResult> {
  if (notification.platform === "fcm") {
    const project = required("FCM_PROJECT_ID");
    if (!/^[a-z0-9-]+$/.test(project)) {
      throw new Error("Invalid FCM project configuration");
    }
    const result = await fetch(
      `https://fcm.googleapis.com/v1/projects/${project}/messages:send`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${await fcmAccessToken()}`,
          "Content-Type": "application/json",
        },
        signal: AbortSignal.timeout(10_000),
        body: JSON.stringify({
          message: {
            token: notification.token,
            notification: {
              title: notification.title,
              body: notification.body,
            },
            data: {
              ...notification.data,
              notification_id: notification.notification_id,
            },
            android: {
              notification: {
                channel_id: "loom_household",
                tag: notification.notification_id,
              },
            },
          },
        }),
      },
    );
    if (result.ok) {
      await result.body?.cancel();
      return "sent";
    }
    const body = await result.json().catch(() => ({}));
    return body.error?.details?.some((detail: { errorCode?: string }) =>
        detail.errorCode === "UNREGISTERED"
      )
      ? "dead"
      : "retry";
  }
  const sandbox = Deno.env.get("APNS_ENVIRONMENT") === "sandbox";
  const result = await fetch(
    `https://${
      sandbox ? "api.sandbox.push.apple.com" : "api.push.apple.com"
    }/3/device/${encodeURIComponent(notification.token)}`,
    {
      method: "POST",
      signal: AbortSignal.timeout(10_000),
      headers: {
        Authorization: `bearer ${await apnsAccessToken()}`,
        "apns-topic": Deno.env.get("APNS_BUNDLE_ID") ?? "com.eyther.lumbridge",
        "apns-push-type": "alert",
        "apns-priority": "10",
        "apns-collapse-id": notification.notification_id,
      },
      body: JSON.stringify({
        aps: {
          alert: { title: notification.title, body: notification.body },
          sound: "default",
          "thread-id": notification.data.household_id,
        },
        ...notification.data,
        notification_id: notification.notification_id,
      }),
    },
  );
  if (result.ok) {
    await result.body?.cancel();
    return "sent";
  }
  const body = await result.json().catch(() => ({}));
  return result.status === 410 || body.reason === "BadDeviceToken" ||
      body.reason === "Unregistered"
    ? "dead"
    : "retry";
}
