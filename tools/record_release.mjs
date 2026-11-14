import { readFileSync } from 'node:fs';
import { ReleaseHistory } from './release_history.mjs';

try {
  const version = JSON.parse(readFileSync(`${process.env.RUNNER_TEMP}/loom-planned-release.json`, 'utf8'));
  const destination = process.env.LOOM_DESTINATION;
  const url = destination === 'firebase'
    ? JSON.parse(readFileSync(`${process.env.RUNNER_TEMP}/loom-upload-result.json`, 'utf8')).url
    : 'https://play.google.com/console/';
  await new ReleaseHistory(process.env.GITHUB_REPOSITORY, process.env.GH_TOKEN).record({ ...version, destination, sha: process.env.GITHUB_SHA, url });
} catch {
  process.stderr.write('The binary was uploaded, but its version history could not be recorded. Check GitHub deployments: write permission before the next release.\n');
  process.exitCode = 1;
}
