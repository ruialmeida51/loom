# Android releases

The **Play Store release** workflow builds `com.eyther.lumbridge` with the existing upload key. It can save a signed AAB or upload it as an **internal-track draft**. It never promotes a production rollout or makes a draft available to testers.

## Credentials

Add these under [repository Actions secrets](https://github.com/ruialmeida51/loom/settings/secrets/actions). Do not paste private credentials into issues, source files, or chat.

| Secret | Content |
| --- | --- |
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | The complete Base64-encoded upload `.jks` file. |
| `ANDROID_UPLOAD_STORE_PASSWORD` | The password that opens the keystore. |
| `ANDROID_UPLOAD_KEY_ALIAS` | The alias of its `PrivateKeyEntry`. |
| `ANDROID_UPLOAD_KEY_PASSWORD` | The password that unlocks that alias's private key; often the same as the store password. |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | The complete, unchanged Google service-account JSON file with Play permissions. |
| `GOOGLE_SERVICES_JSON` | The complete Android Firebase client configuration for `com.eyther.lumbridge`. |

On Linux, inspect the existing key without setting terminal environment variables:

```sh
keytool -list -v -keystore ~/Downloads/keystore.jks
```

Enter the store password at the prompt. Copy **Alias name** from the entry whose type is **PrivateKeyEntry**. To test that the private key can be unlocked, replace `YOUR_ALIAS` below with that alias:

```sh
keytool -certreq -keystore ~/Downloads/keystore.jks -alias YOUR_ALIAS -file /tmp/loom-key-test.csr
rm -f /tmp/loom-key-test.csr
```

The first password opens the store. A second prompt, when needed, asks for the private-key password. If the command succeeds without a second password, the store password also unlocks this key.

Encode the file directly:

```sh
base64 -w0 ~/Downloads/keystore.jks > ~/Downloads/keystore-base64.txt
chmod 600 ~/Downloads/keystore-base64.txt
xdg-open ~/Downloads/keystore-base64.txt
```

Copy all of that text into `ANDROID_UPLOAD_KEYSTORE_BASE64`. Password secrets contain their plain password, without quotes or Base64 encoding. Remove the extra encoded file after saving the secret:

```sh
rm -f ~/Downloads/keystore-base64.txt
```

Keep a secure backup of the original keystore. Do not generate a replacement for the app's existing upload key unless you complete Google's upload-key reset process.

## Play and Firebase setup

1. Enable **Google Play Android Developer API** in the service account's Google Cloud project.
2. In **Play Console → Users and permissions**, invite the service account's `client_email`. Give it access to Loom, permission to view app information, and permission to release to testing tracks. Production-release permission is unnecessary for this workflow.
3. The app must already have a manual upload and be enrolled in Play App Signing. Compare the keystore certificate's SHA-256 fingerprint with **Play Console → App integrity → Upload key certificate**.
4. Enable Google Analytics for the existing Firebase project if it is not enabled. Use a short event-data retention period in Analytics; disable Google Signals and advertising integrations for this app. Do not add advertising IDs or household/account identifiers to the event schema.

Play publishing credentials and FCM server credentials have separate purposes. Uploading an AAB does not configure notification delivery from Supabase.

## Public policy pages

Configure **[Settings → Pages](https://github.com/ruialmeida51/loom/settings/pages) → Source: GitHub Actions**. The **Legal pages** workflow publishes the documents from this repository:

| Console field | URL |
| --- | --- |
| Privacy policy | `https://loom.eyther.dev/privacy/` |
| Account deletion | `https://loom.eyther.dev/delete-account/` |
| Terms of use | `https://loom.eyther.dev/terms/` |

The app opens the same URLs from authentication and Settings. Account deletion uses a verified support request to `eytherdeveloper@gmail.com`; it is not an automatic backend deletion endpoint. Follow [the fulfilment runbook](account-deletion.md) when a request arrives.

The pages identify the app as **Loom** and use the dedicated developer email. They do not publish a home address or country. This satisfies the app-identification option in [Play's privacy-policy requirements](https://support.google.com/googleplay/android-developer/answer/10144311); applicable law can still require the controller's legal identity. Play's verified developer profile has [separate public disclosures](https://support.google.com/googleplay/android-developer/answer/13628312), which a policy cannot hide.

## Build and upload

1. Open **[Actions → Play Store release](https://github.com/ruialmeida51/loom/actions/workflows/play-release.yml) → Run workflow** on `main`.
2. Leave **version code** empty to read Play's existing tracks, APKs and bundles and choose the next code. Use an override only if you need a higher code than that. Planning opens and deletes a temporary edit without committing a release.
3. Set the user-facing version, for example `0.2.0`.
4. Leave **publish** unchecked for the first run. The workflow verifies credentials, runs tests, and saves the signed AAB and R8 mapping as `loom-play-<version-code>`.
5. After the build passes and the policy pages are live, run again with **publish** checked to upload an internal draft. Uploads are serialized to reduce version collisions; avoid simultaneous manual Play uploads.
6. Review the draft in Play Console, complete the app declarations, and explicitly start internal testing when ready. Production promotion remains a separate Play Console action.

The workflow validates inputs without printing credentials and removes temporary signing/service-account files even on failure. A build-only run still needs Play access to allocate a valid version code. Downloaded AABs are release artifacts; retain their matching mapping files.

## Console declarations

Use [the data-safety worksheet](data-safety.md) to complete the current Play forms. Add a reviewer account/instructions under **App access**, declare the app's actual audience, and verify the content rating, ads declaration, screenshots and store text. The current Terms specify adults aged 18 or over. GitHub Actions cannot complete these legal declarations for the developer.

Typical failures:

| Failure | Check |
| --- | --- |
| Missing release setting | The exact secret name, especially `ANDROID_UPLOAD_STORE_PASSWORD`. |
| Cannot authenticate/access Loom | Service-account JSON, API enablement, app access and testing-release permissions. |
| Keystore/key cannot be opened | Store password, alias and private-key password; test locally with `keytool`. |
| Wrong signing certificate | Use the app's registered upload key, not a new debug/replacement key. |
| Version already used | Leave the override empty, or choose a code above every Play upload. |
| Public policy verification fails | Publish **Legal pages** and wait for the configured URLs to serve the current policy version. |

Reference: [Google Play Developer API setup](https://developers.google.com/android-publisher/getting_started), [Firebase collection controls](https://firebase.google.com/docs/analytics/configure-data-collection), and [Play account-deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111).
