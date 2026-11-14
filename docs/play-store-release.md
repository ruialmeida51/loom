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
6. Optional: list existing tester group aliases in `distribution/firebase-groups.txt`, one per line. An empty file uploads builds without sending invitations. A manual run can override the groups in its **tester_groups** field.
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
