# Play data-safety worksheet

This describes the current Android implementation. Reconcile it with the current Play Console questions and provider settings before public distribution. Data supplied to Supabase is collected off-device even when other household members cannot see it. Household sharing chosen by a user and processing by service providers have specific exceptions in Google's definition of sharing; read the form's current guidance rather than marking every provider as a sale or advertising recipient.

| Data | Use | Collection choice |
| --- | --- | --- |
| Email address, account ID, display name and Google-provided profile information when using Google sign-in | Authentication, account management, household membership and member identification. | Required for the signed-in service. |
| Shopping lists, reminders, household names, invitations, RSS URLs and read state | Shared household features, feed delivery and personalization within the app. These are user content/other app data; text may include personal information. | Feature-dependent; users choose which content to add. |
| Expense descriptions, amounts, participants, balances, repayments and statement snapshots | Financial recordkeeping and household splitting. No bank account, card number, payment execution or credit assessment is implemented. | Optional financial features. |
| Receipt photos | Optional expense attachments in private Supabase Storage, readable only with the expense’s household access. Photos may themselves contain personal/payment details. They are resized and re-encoded before upload; they are never sent to analytics. | Optional: one photo explicitly selected from the picker or camera. Declare the Photos category. |
| Firebase Messaging installation/registration identifiers, platform and notification preferences | Messaging infrastructure and delivery of requested reminders and household updates. | The SDK initializes with the app and can create identifiers before notification opt-in. Household token registration and notifications are optional; notification permission does not opt into analytics. |
| Firebase app-instance/device identifiers, screen visits, feature-use events and app/device/session information | Product analytics through Firebase, only after explicit consent in Settings. Google can derive approximate location from network information; account/household data and financial amounts are excluded from our event schema. | Optional; collection starts off and can be withdrawn. |
| Request metadata and infrastructure security logs, including IP information | Operation, abuse prevention and security through hosting providers. | Necessary service operation; verify actual provider retention and logging configuration. |

Declare applicable **Personal info**, **Financial info**, **Photos and videos** (photos only), **App activity**, **Device or other IDs** and, where Google's SDK guidance requires it, **Approximate location** categories. Do not claim that pseudonymous identifiers or approximate location are anonymous merely because Loom omits account IDs. Use Google's current [Firebase Android data-disclosure guidance](https://firebase.google.com/docs/android/play-data-disclosure) when answering the SDK-specific fields.

The app has no ads. The manifest removes advertising/AdServices permissions and disables advertising-ID, SSAID, automatic screen reporting and ad-personalization collection. Firebase ad-storage, ad-user-data and ad-personalization consent remain denied when usage analytics is enabled. Custom events accept only enums; they cannot carry free text, amounts, URLs or account identifiers.

Account and household data travel over HTTPS to Supabase. Device session credentials use platform-protected storage. Household authorization depends on current membership and server-side policies. Do not describe this as end-to-end encryption.

Provide both the public Privacy Policy and account-deletion URL listed in [release setup](play-store-release.md). The app links to the same support-assisted deletion process from authentication and Settings. Requests must actually be fulfilled; the public page alone does not delete data. See [the runbook](account-deletion.md) for the current operational process and retained shared records.

Opting out stops subsequent Android analytics collection and resets local Firebase analytics data. It does not retroactively remove aggregate reports or all historical Google data. Confirm the Analytics property's retention settings and handle historical rights requests with Google's supported tools. iOS currently does not include Firebase Analytics.

Review [Play's definitions](https://support.google.com/googleplay/android-developer/answer/10787469) whenever the app's data uses, providers or monetization change.

Receipt uploads normalize orientation, resize and re-encode to JPEG. Original image metadata is not
forwarded. Android uses the system picker/external camera; iOS declares its camera purpose in the
platform resource. Photos and receipt paths never enter analytics. Confirm the Photos and videos
category in Play Console before a Play release; Firebase remains the current distribution target.

Receipt permissions use the system image picker and an external camera on Android; the app does not request broad photo-library access. iOS camera access has a purpose string. Removing a receipt or voiding/deleting its expense unlinks it and queues Storage API cleanup. Unattached interrupted uploads are collected after 24 hours. Record the optional Photos collection in Play Console before a public Play release.
