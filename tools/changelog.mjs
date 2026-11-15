import { appendFileSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';

const headingPattern = /^## \[([^\]]+)\](?: - (\d{4}-\d{2}-\d{2}))?[ \t]*$/gm;
const versionPattern = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/;

function sections(markdown) {
  const headings = [...markdown.matchAll(headingPattern)];
  return headings.map((heading, index) => {
    const end = headings[index + 1]?.index ?? markdown.length;
    const lines = markdown.slice(heading.index + heading[0].length, end).trim().split('\n').filter(line => line.trim());
    if (lines.some(line => !/^- \S/.test(line))) throw new Error('Changelog entries must use one bullet per line.');
    return { label: heading[1], start: heading.index, end, entries: lines.map(line => line.slice(2).trimEnd()) };
  });
}

function unreleasedSection(all) {
  const matches = all.filter(section => section.label === 'Unreleased');
  if (matches.length !== 1) throw new Error('Changelog needs exactly one [Unreleased] section.');
  return matches[0];
}

function bullets(entries) { return entries.map(entry => `- ${entry}`).join('\n'); }

function validateRelease(release) {
  if (!versionPattern.test(release.versionName ?? '') || !Number.isSafeInteger(release.versionCode) || release.versionCode < 1) {
    throw new Error('Changelog release needs a valid version name and build number.');
  }
  if (!/^\d{4}-\d{2}-\d{2}$/.test(release.date ?? '') || !Array.isArray(release.entries) || !release.entries.length ||
      release.entries.some(entry => typeof entry !== 'string' || !entry.trim() || /[\r\n]/.test(entry))) {
    throw new Error('Changelog release needs a date and nonempty single-line entries.');
  }
  if (bullets(release.entries).length + 1 > 500) throw new Error('Release notes exceed the Play limit of 500 characters.');
}

/** Freeze notes from the source commit, never from a newer main commit that might land during a build. */
export function prepareChangelog(markdown, { versionName, versionCode }, date) {
  const all = sections(markdown);
  const unreleased = unreleasedSection(all);
  const entries = unreleased.entries.length ? unreleased.entries : all.find(section => versionPattern.test(section.label))?.entries;
  const release = { versionName, versionCode, date, entries };
  validateRelease(release);
  return release;
}

export function renderReleaseNotes(release) {
  validateRelease(release);
  return `${bullets(release.entries)}\n`;
}

/** Keep notes added during the upload, and leave an existing release immutable on retries. */
export function archiveChangelog(markdown, release) {
  validateRelease(release);
  const all = sections(markdown);
  const unreleased = unreleasedSection(all);
  const existing = all.find(section => section.label === release.versionName);
  if (existing && JSON.stringify(existing.entries) !== JSON.stringify(release.entries)) {
    throw new Error('Changelog already contains different notes for this version.');
  }
  const released = new Set(release.entries);
  const remaining = unreleased.entries.filter(entry => !released.has(entry));
  if (existing && remaining.length === unreleased.entries.length) return markdown;
  const next = `## [Unreleased]\n\n${remaining.length ? `${bullets(remaining)}\n\n` : ''}`;
  const archived = existing ? '' : `## [${release.versionName}] - ${release.date}\n\n${bullets(release.entries)}\n\n`;
  return `${markdown.slice(0, unreleased.start)}${next}${archived}${markdown.slice(unreleased.end).trimStart()}`;
}

/** The Contents API's file SHA protects concurrent edits; only CHANGELOG.md can be written. */
export async function recordChangelog({ repository, token, release, request = fetch }) {
  validateRelease(release);
  if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repository ?? '') || !token) throw new Error('GitHub changelog credentials are missing.');
  const url = `https://api.github.com/repos/${repository}/contents/CHANGELOG.md`;
  const headers = { authorization: `Bearer ${token}`, accept: 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28', 'content-type': 'application/json' };
  for (let attempt = 0; attempt < 3; attempt++) {
    const current = await request(`${url}?ref=main`, { headers, signal: AbortSignal.timeout(30_000) });
    if (!current.ok) throw new Error(`GitHub changelog read failed (HTTP ${current.status}).`);
    const file = await current.json();
    if (file.type !== 'file' || file.encoding !== 'base64' || !file.sha) throw new Error('GitHub changelog response is invalid.');
    const before = Buffer.from(file.content, 'base64').toString('utf8');
    const after = archiveChangelog(before, release);
    if (after === before) return;
    const updated = await request(url, {
      method: 'PUT', headers, signal: AbortSignal.timeout(30_000),
      body: JSON.stringify({ message: `docs: record Loom ${release.versionName} changelog`, branch: 'main', sha: file.sha, content: Buffer.from(after).toString('base64') }),
    });
    if (updated.ok) return;
    if (updated.status !== 409) throw new Error(`GitHub changelog update failed (HTTP ${updated.status}).`);
  }
  throw new Error('GitHub changelog changed repeatedly. The binary is uploaded; retry recording its changelog.');
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const directory = process.env.RUNNER_TEMP;
    if (!directory) throw new Error('Changelog preparation needs RUNNER_TEMP.');
    if (process.argv[2] === 'prepare') {
      const version = JSON.parse(readFileSync(join(directory, 'loom-planned-release.json'), 'utf8'));
      const release = prepareChangelog(readFileSync('CHANGELOG.md', 'utf8'), version, new Date().toISOString().slice(0, 10));
      mkdirSync(join(directory, 'whatsnew'), { recursive: true });
      writeFileSync(join(directory, 'loom-release-notes.json'), JSON.stringify(release));
      writeFileSync(join(directory, 'whatsnew/whatsnew-en-US'), renderReleaseNotes(release));
      writeFileSync(join(directory, 'loom-release-changelog.md'), `# Loom ${release.versionName} (${release.versionCode})\n\n${renderReleaseNotes(release)}\nSource: \`${process.env.GITHUB_SHA}\`\n`);
      process.stdout.write(`Prepared changelog for Loom ${release.versionName} (${release.versionCode}).\n`);
    } else if (process.argv[2] === 'record') {
      const release = JSON.parse(readFileSync(join(directory, 'loom-release-notes.json'), 'utf8'));
      await recordChangelog({ repository: process.env.GITHUB_REPOSITORY, token: process.env.GH_TOKEN, release });
      appendFileSync(process.env.GITHUB_STEP_SUMMARY, `Recorded Loom ${release.versionName} in CHANGELOG.md.\n`);
    } else throw new Error('Changelog command must be prepare or record.');
  } catch (error) {
    process.stderr.write(/^(Changelog |Release notes |GitHub changelog )/.test(error.message ?? '') ? `${error.message}\n` : 'Changelog processing failed. Check its format and workflow permissions.\n');
    process.exitCode = 1;
  }
}
