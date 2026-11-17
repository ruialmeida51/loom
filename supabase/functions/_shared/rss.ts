import { XMLParser } from "fast-xml-parser";

const requiredHosts = ["rss.impresa.pt"];
export const defaultHosts = [
  "feeds.bbci.co.uk",
  "www.theguardian.com",
  "hnrss.org",
  "feeds.arstechnica.com",
  "feeds.feedburner.com",
  "www.nasa.gov",
  ...requiredHosts,
];
export const maxFeedBytes = 1_048_576;

/** Preserve configured restrictions while adding the publisher explicitly supported by this release. */
export function allowedFeedHosts(configured?: string): string[] {
  const hosts = (configured ?? defaultHosts.join(",")).split(",")
    .map((host) => host.trim().toLowerCase()).filter(Boolean);
  return [...new Set([...hosts, ...requiredHosts])];
}

/** Fetches are restricted to explicitly trusted publishers, including every redirect hop. */
export function trustedFeedUrl(value: string, hosts: string[]): URL {
  const url = new URL(value);
  if (
    url.protocol !== "https:" || url.username || url.password ||
    (url.port && url.port !== "443") ||
    !hosts.includes(url.hostname.toLowerCase())
  ) {
    throw new Error("This publisher is not supported yet");
  }
  return url;
}

export async function fetchFeed(
  value: string,
  hosts: string[],
): Promise<string> {
  let url = trustedFeedUrl(value, hosts);
  for (let hop = 0; hop < 4; hop++) {
    const result = await fetch(url, {
      redirect: "manual",
      signal: AbortSignal.timeout(10_000),
      headers: {
        "Accept":
          "application/rss+xml, application/atom+xml, text/xml, application/xml",
        "User-Agent": "Loom/0.1",
      },
    });
    if ([301, 302, 303, 307, 308].includes(result.status)) {
      const location = result.headers.get("location");
      await result.body?.cancel();
      if (!location) throw new Error("The feed redirected without a location");
      url = trustedFeedUrl(new URL(location, url).href, hosts);
      continue;
    }
    if (!result.ok || !result.body) {
      await result.body?.cancel();
      throw new Error("The publisher could not be reached");
    }
    if (Number(result.headers.get("content-length")) > maxFeedBytes) {
      await result.body.cancel();
      throw new Error("The feed is too large");
    }
    const reader = result.body.getReader();
    const chunks: Uint8Array[] = [];
    let length = 0;
    try {
      while (true) {
        const part = await reader.read();
        if (part.done) break;
        length += part.value.byteLength;
        if (length > maxFeedBytes) {
          await reader.cancel();
          throw new Error("The feed is too large");
        }
        chunks.push(part.value);
      }
    } finally {
      reader.releaseLock();
    }
    const bytes = new Uint8Array(length);
    let offset = 0;
    for (const chunk of chunks) {
      bytes.set(chunk, offset);
      offset += chunk.length;
    }
    return new TextDecoder().decode(bytes);
  }
  throw new Error("Too many feed redirects");
}

type XmlRecord = Record<string, unknown>;
function record(value: unknown): XmlRecord {
  return value && typeof value === "object" ? value as XmlRecord : {};
}
function list(value: unknown): unknown[] {
  return value === undefined ? [] : Array.isArray(value) ? value : [value];
}
function text(value: unknown): string {
  if (typeof value === "string" || typeof value === "number") {
    return String(value);
  }
  return String(record(value)["#text"] ?? "");
}
export function plainText(value: unknown): string {
  return text(value).replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(
    /&amp;/g,
    "&",
  ).replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/\s+/g, " ").trim();
}
export interface FeedArticle {
  guid: string;
  title: string;
  summary: string;
  url: string;
  source: string;
  published_at: string;
}

export function parseFeed(
  xml: string,
  feedUrl: string,
  now = new Date(),
): FeedArticle[] {
  if (
    new TextEncoder().encode(xml).length > maxFeedBytes ||
    /<!DOCTYPE|<!ENTITY/i.test(xml)
  ) throw new Error("Unsupported feed document");
  const parsed = new XMLParser({
    ignoreAttributes: false,
    processEntities: false,
    ignoreDeclaration: true,
  }).parse(xml);
  const rss = record(record(parsed).rss);
  const channel = record(rss.channel);
  const atom = record(record(parsed).feed);
  const isAtom = Object.keys(atom).length > 0;
  const root = isAtom ? atom : channel;
  if (!Object.keys(root).length) {
    throw new Error("This URL does not contain an RSS or Atom feed");
  }
  const source = plainText(root.title).slice(0, 120) ||
    new URL(feedUrl).hostname;
  const result: FeedArticle[] = [];
  const seen = new Set<string>();
  for (const item of list(isAtom ? root.entry : root.item).slice(0, 100)) {
    const entry = record(item);
    const rawLink = isAtom
      ? text(
        record(
          list(entry.link).find((link) =>
            !record(link)["@_rel"] || record(link)["@_rel"] === "alternate"
          ),
        )["@_href"],
      )
      : text(entry.link);
    let url: URL;
    try {
      url = new URL(rawLink, feedUrl);
    } catch {
      continue;
    }
    if (!rawLink || url.protocol !== "https:" || url.username || url.password) {
      continue;
    }
    const title = plainText(entry.title).slice(0, 500);
    if (!title) continue;
    const guid = (text(isAtom ? entry.id : entry.guid) || url.href).slice(
      0,
      4096,
    );
    if (seen.has(guid)) continue;
    seen.add(guid);
    const date = new Date(
      text(isAtom ? entry.published ?? entry.updated : entry.pubDate),
    );
    result.push({
      guid,
      title,
      url: url.href,
      source,
      summary: plainText(
        entry.description ?? entry.summary ?? entry.content ??
          entry["content:encoded"],
      ).slice(0, 1000),
      published_at: Number.isFinite(date.getTime())
        ? date.toISOString()
        : now.toISOString(),
    });
  }
  return result;
}
