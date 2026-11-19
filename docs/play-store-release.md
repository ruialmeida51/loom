# Android releases

The **Android release** workflow builds `com.eyther.lumbridge` with the existing upload key. App changes on `main` go to **Firebase App Distribution**. An explicit **Play** run uploads an **internal-track draft**, then creates the matching GitHub release and `v<version>` tag. Firebase builds create neither a GitHub release nor a tag. Production promotion remains a separate Play Console action.

Release versions start at **4.0.0**. Each successful Firebase or Play release advances the same patch sequence: `4.0.0`, `4.0.1`, `4.0.2`. Android build codes also increase across both destinations. The planner reads Play uploads, Firebase builds and successful GitHub deployment metadata, so Firebase's expiry of older builds does not reset the counter. Planning alone does not consume a version. Uploads share a serial queue; avoid concurrent manual uploads outside the workflow.

`loomVersionName` in `gradle.properties` is the minimum release version and the local development version. Raise that floor when starting a new major/minor series. iOS marketing metadata also starts at 4.0.0; iOS distribution is not configured by this Android workflow.

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
4. In Google Cloud project **lumbridge-app**, enable the **Firebase App Distribution API** (`firebaseappdistribution.googleapis.com`). Grant the same service account **Firebase App Distribution Admin** (`roles/firebaseappdistro.admin`) on that project. This is a Firebase/GCP permission in addition to its separate Play Console permissions; GCP Editor and domain-wide delegation are unnecessary.
5. Open Firebase Console → **App Distribution** for the existing Android app `com.eyther.lumbridge` and complete its initial setup. The workflow reads its app ID from `GOOGLE_SERVICES_JSON`; no new secret or CLI login token is needed.
6. Firebase releases distribute automatically to the existing **internal-testers** group configured in `distribution/firebase-groups.txt`. A manual run can override the aliases in its **tester_groups** field. Keep the group alias aligned with Firebase Console; an intentionally empty aliases file uploads without invitations.
7. Enable Google Analytics for the existing Firebase project if it is not enabled. Use a short event-data retention period in Analytics; disable Google Signals and advertising integrations for this app. Do not add advertising IDs or household/account identifiers to the event schema.

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

1. Open **[Actions → Android release](https://github.com/ruialmeida51/loom/actions/workflows/play-release.yml) → Run workflow**.
2. Choose **check** to verify the service-account connections and calculate the next version without building, uploading, tagging or consuming that version. This can run on a feature branch before merging workflow changes.
3. Choose **firebase** on `main` to upload a signed APK. App changes merged to `main` do this automatically. Tester groups come from the committed aliases file unless the manual run supplies an override.
4. Choose **play** on `main` to upload a signed AAB as an internal draft. After a successful upload, the workflow creates GitHub prerelease `v<version>` at the exact source commit and attaches the AAB. The release notes identify the internal-draft status. The corresponding R8 mapping is retained in the Actions artifact.
5. Leave **version_code** empty unless an external upload requires a higher override. User-facing versions are automatic.
6. Review a Play draft in Play Console, complete the app declarations, and explicitly start internal testing when ready. Production rollout remains manual.

The workflow runs tests before packaging, validates inputs without printing credentials, and removes private signing/service-account files even on failure. The release-history records are deployment metadata; they do not publish a GitHub release for Firebase builds.

Firebase APKs use the upload certificate. When a Play installation uses a different app-signing certificate, Android requires removing that installation before installing the Firebase APK.

## Console declarations

Use [the data-safety worksheet](data-safety.md) to complete the current Play forms. Add a reviewer account/instructions under **App access**, declare the app's actual audience, and verify the content rating, ads declaration, screenshots and store text. The current Terms specify adults aged 18 or over. GitHub Actions cannot complete these legal declarations for the developer.

Typical failures:

| Failure | Check |
| --- | --- |
| Missing release setting | The exact secret name, especially `ANDROID_UPLOAD_STORE_PASSWORD`. |
| Firebase access denied | Enable the App Distribution API and grant Firebase App Distribution Admin on lumbridge-app. Run **check** again. |
| Cannot authenticate/access Loom | Service-account JSON, API enablement, app access and testing-release permissions. |
| Keystore/key cannot be opened | Store password, alias and private-key password; test locally with `keytool`. |
| Wrong signing certificate | Use the app's registered upload key, not a new debug/replacement key. |
| Version already used | Leave the override empty, or choose a code above every Play and Firebase upload. |
| Public policy verification fails | Publish **Legal pages** and wait for the configured URLs to serve the current policy version. |

Reference: [Google Play Developer API setup](https://developers.google.com/android-publisher/getting_started), [Firebase collection controls](https://firebase.google.com/docs/analytics/configure-data-collection), and [Play account-deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111).

## Changelog and release notes

Edit user-facing bullets under `[Unreleased]` in [CHANGELOG.md](../CHANGELOG.md). Use one bullet per line and keep the combined notes within Play’s 500-character limit. The [Loom release skill](../.agents/skills/loom-release/SKILL.md) describes the complete workflow.

The workflow freezes these notes from the release source commit, uses them for Firebase and Play, and saves a versioned Markdown snapshot as an artifact. Successful deployment history also retains the notes. After upload, the bot archives the actual allocated version in CHANGELOG.md through a file-SHA-protected update. New bullets merged while the build ran stay Unreleased. Changelog-only commits do not start another release. Promotions or rebuilds with no new bullets reuse the latest released notes.

Version allocation remains automatic across both destinations. Do not manually bump the bootstrap `loomVersionName` for each patch, duplicate notes in a distribution file, or add a Firebase Git tag. Failed uploads and connection checks do not archive the changelog.


### iOS Firebase and crash diagnostics

Loom iOS is registered in Firebase project `lumbridge-app` with bundle ID `com.eyther.lumbridge`.
`iosApp/iosApp/GoogleService-Info.plist` is the matching ignored mobile client configuration.
CI fetches the existing client with the service account already configured for releases; it never
prints credentials or requires another repository secret. Local builds can download the matching
Apple configuration from Firebase Console. Without it, Firebase remains unavailable in that build. Firebase Apple SDK 12.9.0 is pinned through Swift Package Manager. Collection defaults
off in Info.plist; Swift bridges expose consent and Kotlin fatal frames without setting a user ID.
Release device builds use Firebase's recommended Crashlytics `run` phase with Xcode's required
dSYM, built Firebase configuration and executable inputs. Debug builds retain optional Firebase
setup. CI additionally creates an unsigned device Release archive, checks that its dSYM UUIDs
cover the app binary, synchronously uploads the archive's complete symbol set with `upload-symbols`,
and retains `Loom-dSYMs.zip`, archive metadata and UUIDs for 90 days.
`LoomFoundation` is statically linked, so its code belongs to the app's release symbol set.

Android explicitly enables and runs `uploadCrashlyticsMappingFileRelease`. Before Firebase or Play
distribution, `tools/verify_android_symbols.py` checks the R8 map ID against the APK's DEX source
markers, the Crashlytics mapping ID against its generated metadata/resources and packaged value,
and the package/version against the release plan. Every release retains the APK/AAB, final mapping,
Crashlytics ID and `symbols.json` (source commit, both IDs and SHA-256 hashes) in the versioned
`loom-android-symbols-<version>-<code>` Actions artifact for 90 days, including Firebase builds.
Retrieve this artifact immediately for an affected build and keep it with long-lived release
records if it must remain diagnosable beyond Actions retention. A crash's `r8-map-id-…` must equal
the mapping's `pg_map_id` before retracing it. The random Crashlytics mapping ID is a separate ID.

The documented configurations come from [Firebase's Android mapping guide](https://firebase.google.com/docs/crashlytics/android/get-deobfuscated-reports)
and [Apple symbol guide](https://firebase.google.com/docs/crashlytics/ios/get-deobfuscated-reports).

The idempotent Configure Loom iOS Firebase workflow can retrieve the client configuration again
using the existing service account. It does not upload a build, consume a version or change consent.
Confirm symbolicated reports in Firebase after a consenting test installation reports a crash;
compilation alone does not prove crash-event delivery. Physical iOS permission and resume behaviour
still needs Apple hardware. APNs delivery continues to use the existing Supabase push setup.
