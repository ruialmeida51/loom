import { appendFileSync, readFileSync, writeFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import { plan as planPlay } from './plan_play_release.mjs';
import { FirebaseDistribution } from './firebase_distribution.mjs';
import { ReleaseHistory } from './release_history.mjs';

function semver(value) {
  const match = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/.exec(value ?? '');
  if (!match) return null;
  const parts = match.slice(1).map(Number);
  return parts.every(Number.isSafeInteger) ? parts : null;
}
const compare = (left, right) => left[0] - right[0] || left[1] - right[1] || left[2] - right[2];

export function nextVersionName(minimum, used) {
  const floor = semver(minimum);
  if (!floor) throw new Error('The base loomVersionName must use major.minor.patch.');
  const versions = used.map(semver).filter(Boolean).filter(version => compare(version, floor) >= 0);
  if (!versions.length) return minimum;
  const maximum = versions.reduce((left, right) => compare(left, right) >= 0 ? left : right);
  if (!Number.isSafeInteger(maximum[2] + 1)) throw new Error('The release patch version is too large.');
  return `${maximum[0]}.${maximum[1]}.${maximum[2] + 1}`;
}

export function nextReleaseVersion({ minimum, play, firebase, history, requestedCode = '' }) {
  const usedCodes = [play.versionCode - 1, ...firebase.map(release => Number(release.buildVersion)), ...history.map(release => release.versionCode)];
  const maximum = Math.max(0, ...usedCodes.filter(code => Number.isSafeInteger(code) && code > 0));
  if (requestedCode && !/^[1-9]\d*$/.test(requestedCode)) throw new Error('Choose a version code using positive decimal digits only.');
  const versionCode = requestedCode ? Number(requestedCode) : maximum + 1;
  if (!Number.isSafeInteger(versionCode) || versionCode <= maximum || versionCode > 2_100_000_000) {
    throw new Error(`Choose a version code higher than ${maximum} and below 2100000001.`);
  }
  return {
    versionCode,
    versionName: nextVersionName(minimum, [...play.versionNames, ...firebase.map(release => release.displayVersion), ...history.map(release => release.versionName)]),
  };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const [credentialPath, configPath] = process.argv.slice(2);
    const account = JSON.parse(readFileSync(credentialPath, 'utf8'));
    const config = JSON.parse(readFileSync(configPath, 'utf8'));
    const minimum = /^loomVersionName=(.+)$/m.exec(readFileSync('gradle.properties', 'utf8'))?.[1]?.trim();
    // Finish and close the read-only planning edit before querying any other service.
    const play = await planPlay(account);
    const firebase = await FirebaseDistribution.connect(account, config.firebaseApp);
    const history = new ReleaseHistory(process.env.GITHUB_REPOSITORY, process.env.GH_TOKEN);
    const [firebaseReleases, previous] = await Promise.all([firebase.releases(), history.versions()]);
    const version = nextReleaseVersion({ minimum, play, firebase: firebaseReleases, history: previous, requestedCode: process.env.LOOM_REQUESTED_VERSION_CODE });
    appendFileSync(process.env.GITHUB_OUTPUT, `version_name=${version.versionName}\nversion_code=${version.versionCode}\n`);
    writeFileSync(`${process.env.RUNNER_TEMP}/loom-planned-release.json`, JSON.stringify(version));
    process.stdout.write(`Next Loom release: ${version.versionName} (${version.versionCode})\n`);
  } catch (error) {
    const safe = /^(Google |Cannot access Loom|Play returned|Could not|Choose a version|Firebase |GitHub release-history|The base |The release )/.test(error.message ?? '');
    process.stderr.write(safe ? `${error.message}\n` : 'Release planning failed. Verify the Google service-account key, Firebase configuration and workflow permissions.\n');
    process.exitCode = 1;
  }
}
