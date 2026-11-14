import { appendFileSync, readFileSync, writeFileSync } from 'node:fs';
import { basename } from 'node:path';
import { FirebaseDistribution } from './firebase_distribution.mjs';

try {
  const [credentialPath, configPath, apkPath, notesPath] = process.argv.slice(2);
  const account = JSON.parse(readFileSync(credentialPath, 'utf8'));
  const config = JSON.parse(readFileSync(configPath, 'utf8'));
  const client = await FirebaseDistribution.connect(account, config.firebaseApp);
  const configuredGroups = process.env.LOOM_FIREBASE_GROUPS?.trim() || readFileSync('distribution/firebase-groups.txt', 'utf8');
  const groups = configuredGroups.split(/[,\n]/).map(group => group.trim()).filter(group => group && !group.startsWith('#'));
  if (groups.some(group => !/^[A-Za-z0-9_-]+$/.test(group))) throw new Error('Firebase tester group aliases may contain only letters, digits, underscores and hyphens.');
  const release = await client.upload({ binary: readFileSync(apkPath), fileName: basename(apkPath), notes: readFileSync(notesPath, 'utf8'), groups });
  const url = release.firebaseConsoleUri;
  if (!url || !url.startsWith('https://console.firebase.google.com/')) throw new Error('Firebase did not return a console URL.');
  writeFileSync(`${process.env.RUNNER_TEMP}/loom-upload-result.json`, JSON.stringify({ url }));
  appendFileSync(process.env.GITHUB_STEP_SUMMARY, `Uploaded Loom ${release.displayVersion} (${release.buildVersion}) to [Firebase App Distribution](${url}).\n${groups.length ? 'Distributed to the configured tester groups.' : 'Upload only; no tester invitations were sent.'}\n`);
} catch (error) {
  process.stderr.write(error.message?.startsWith('Firebase ') ? `${error.message}\n` : 'Firebase upload failed. Check the prepared credentials, APK and tester group aliases.\n');
  process.exitCode = 1;
}
