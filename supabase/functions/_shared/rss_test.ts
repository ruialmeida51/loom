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

Deno.test("Pictures come from Media RSS, the widest first", () => {
  const [article] = parseFeed(
    `<rss xmlns:media="http://search.yahoo.com/mrss/"><channel><title>Pics</title><item><title>One</title><link>https://example.org/one</link><media:content url="https://img.example.org/small.jpg?a=1&amp;b=2" width="140" medium="image"/><media:content url="https://img.example.org/large.jpg" width="1200" medium="image"/><media:content url="https://img.example.org/clip.mp4" medium="video"/></item></channel></rss>`,
    "https://example.org/rss",
  );
  assert(article.image_url === "https://img.example.org/large.jpg", article.image_url ?? "none");
});

Deno.test("Pictures fall back to thumbnails, enclosures and the story's first image", () => {
  const articles = parseFeed(
    `<rss xmlns:media="http://search.yahoo.com/mrss/"><channel><title>Pics</title>
      <item><guid>a</guid><title>Thumb</title><link>https://example.org/a</link><media:thumbnail url="https://img.example.org/a.jpg?w=240&amp;q=80" width="240"/></item>
      <item><guid>b</guid><title>Enclosure</title><link>https://example.org/b</link><enclosure url="https://img.example.org/b.png" type="image/png" length="1"/></item>
      <item><guid>c</guid><title>Inline</title><link>https://example.org/c</link><description><![CDATA[<p><img alt="" src="/images/c.jpg"> Words</p>]]></description></item>
      <item><guid>d</guid><title>Insecure</title><link>https://example.org/d</link><enclosure url="http://img.example.org/d.jpg" type="image/jpeg"/></item>
      <item><guid>e</guid><title>Audio</title><link>https://example.org/e</link><enclosure url="https://img.example.org/e.mp3" type="audio/mpeg"/></item>
    </channel></rss>`,
    "https://example.org/rss",
  );
  assert(articles[0].image_url === "https://img.example.org/a.jpg?w=240&q=80", articles[0].image_url ?? "none");
  assert(articles[1].image_url === "https://img.example.org/b.png");
  assert(articles[2].image_url === "https://example.org/images/c.jpg", articles[2].image_url ?? "none");
  assert(articles[2].summary === "Words");
  assert(articles[3].image_url === null && articles[4].image_url === null);
});

Deno.test("Atom pictures come from image enclosures", () => {
  const [article] = parseFeed(
    `<feed><title>Atom</title><entry><id>x</id><title>X</title><link href="https://example.org/x"/><link rel="enclosure" type="image/jpeg" href="https://img.example.org/x.jpg"/></entry></feed>`,
    "https://example.org/atom",
  );
  assert(article.url === "https://example.org/x" && article.image_url === "https://img.example.org/x.jpg");
});

Deno.test("BBC thumbnails are asked for at a readable size", () => {
  const [article] = parseFeed(
    `<rss xmlns:media="http://search.yahoo.com/mrss/"><channel><title>BBC</title><item><title>B</title><link>https://www.bbc.co.uk/news/b</link><media:thumbnail width="240" height="135" url="https://ichef.bbci.co.uk/ace/standard/240/cpsprodpb/abc/live/x.jpg"/></item></channel></rss>`,
    "https://feeds.bbci.co.uk/news/rss.xml",
  );
  assert(article.image_url === "https://ichef.bbci.co.uk/ace/standard/800/cpsprodpb/abc/live/x.jpg", article.image_url ?? "none");
});
