import { allowedFeedHosts, defaultHosts, parseFeed, trustedFeedUrl } from "./rss.ts";
function assert(value: unknown, message = "Assertion failed"): asserts value {
  if (!value) throw new Error(message);
}
function rejects(action: () => unknown) {
  let rejected = false;
  try {
    action();
  } catch {
    rejected = true;
  }
  assert(rejected);
}

Deno.test("Expresso is supported without opening the publisher allowlist", () => {
  assert(trustedFeedUrl("https://rss.impresa.pt/feed/latest/expresso.rss", defaultHosts).hostname === "rss.impresa.pt");
  rejects(() => trustedFeedUrl("https://rss.impresa.pt.evil.test/feed", defaultHosts));
  rejects(() => trustedFeedUrl("https://127.0.0.1/feed", defaultHosts));
  rejects(() => trustedFeedUrl("https://rss.impresa.pt:8443/feed", defaultHosts));
});

Deno.test("Existing configured restrictions retain only their publishers plus the approved addition", () => {
  const hosts = allowedFeedHosts(" feeds.bbci.co.uk,FEEDS.BBCI.CO.UK ");
  assert(hosts.length === 2);
  assert(hosts.includes("feeds.bbci.co.uk") && hosts.includes("rss.impresa.pt"));
  rejects(() => trustedFeedUrl("https://www.nasa.gov/feed/", hosts));
  rejects(() => trustedFeedUrl("https://rss.impresa.pt.evil.test/feed", hosts));
});

Deno.test("RSS parses plain text, valid links, and stable GUIDs", () => {
  const articles = parseFeed(
    `<rss><channel><title>Our feed</title><item><guid>one</guid><title>Hello</title><description><![CDATA[<p>Good <b>morning</b></p>]]></description><link>https://example.org/one</link></item><item><guid>one</guid><title>Duplicate</title><link>https://example.org/two</link></item></channel></rss>`,
    "https://example.org/rss",
  );
  assert(
    articles.length === 1 && articles[0].summary === "Good morning" &&
      articles[0].guid === "one",
  );
});
Deno.test("Atom selects alternate links and relative URLs", () => {
  const articles = parseFeed(
    `<feed><title>Atom</title><entry><id>one</id><title>A story</title><link rel="self" href="https://example.org/atom/one"/><link rel="alternate" href="/story"/></entry></feed>`,
    "https://example.org/feed",
  );
  assert(articles[0].url === "https://example.org/story");
});
Deno.test("Untrusted protocols, userinfo, ports, and publishers are refused", () => {
  for (
    const value of [
      "http://example.org/feed",
      "https://127.0.0.1/feed",
      "https://example.org:8443/feed",
      "https://name:password@example.org/feed",
      "https://example.org.evil.test/feed",
    ]
  ) rejects(() => trustedFeedUrl(value, ["example.org"]));
  assert(
    trustedFeedUrl("https://example.org/feed", ["example.org"]).hostname ===
      "example.org",
  );
});
Deno.test("DTD and active article URLs are refused", () => {
  rejects(() =>
    parseFeed(
      `<!DOCTYPE rss [<!ENTITY a "data">]><rss/>`,
      "https://example.org/feed",
    )
  );
  const articles = parseFeed(
    `<rss><channel><title>Feed</title><item><title>No</title><link>javascript:alert(1)</link></item></channel></rss>`,
    "https://example.org/feed",
  );
  assert(articles.length === 0);
});
