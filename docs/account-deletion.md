# Account-deletion requests

The public deletion page and in-app link direct users to `eytherdeveloper@gmail.com`. This is a **manual, verified support process**, not an automatic deletion API. Monitor the mailbox and normally complete verified requests within 30 days, or explain a lawful extension. Google's requirements allow a customer-service email resource, but the developer must perform the deletion.

## Verify and scope the request

1. Verify control of the account email without requesting passwords or tokens. Do not delete an account solely because a stranger knows its email. Record the request and the minimum evidence needed for fulfilment.
2. Identify the account UUID and all memberships using privileged operator access. Identify owned/shared households, recurring schedules, personal push registrations/preferences, read states and references in stored statements. Discuss ownership transfer for households with remaining members.
3. Explain any specific lawful retention exception before retaining identifiable records. Other members' convenience alone does not establish an exception. Pseudonymization still requires a lawful reason for retention. Discuss redaction/removal where the requester supplied personal information inside shared descriptions.

## Fulfil with privileged operator tools

The schema deliberately preserves financial integrity: `profiles.id` references `auth.users.id` with `ON DELETE RESTRICT`, and historical financial rows reference household members. A blind hard delete of the Auth user will fail or break the intended retention process. Never bypass foreign keys, disable integrity triggers globally, or place a service-role key in the mobile app.

Perform the following through a reviewed server-side operation or the Supabase administrative tools. Prepare a transaction for database changes, verify row counts, and avoid printing personal records into CI logs:

1. Transfer ownership where agreed, or delete a sole-member household and its dependent records in foreign-key order. Pause/cancel schedules that should no longer generate records. Do not delete another household member's data without a lawful basis.
2. Revoke sessions/block new sign-ins and deactivate the departing user's memberships. Existing access JWTs can remain valid until expiry; membership checks must deny household access immediately, and any remaining personal endpoints must be considered before confirming deletion.
3. Remove `push_devices` (including dependent delivery records), `notification_preferences`, `article_reads`, outstanding invitations and other user-specific data. Search private operational tables as well as public tables for references and notification payloads.
4. Remove identifying profile information. Where historical records must lawfully remain, use a generic deleted-member label and inactive membership, with no original display name or email. Review user-authored text and notification payloads separately; changing the profile alone does not redact copies.
5. Review receipt photos supplied by the requester. Clear appropriate `expense_receipts.object_path` links while incrementing their version tokens, then verify the Storage cleanup queue is drained. Use the Storage API to remove files; deleting `storage.objects` rows alone does not delete physical blobs. Explain any lawful retention of shared receipt content.
6. Inspect `monthly_statements.payload`: stored member names are snapshots and do not update when a profile changes. Redact identifying copies through a narrowly scoped, reviewed administrative migration/process that preserves the statement integrity contract and recomputes the content hash. Existing exports held by other users cannot be recalled.
7. For retained foreign-key references, use Supabase Auth's supported **soft-delete** administrative option after checking the current API behavior. It removes access/identifying Auth fields while preserving the referenced row identifier. Do not substitute the ordinary hard-delete button for this step. If all references are lawfully removed, a hard delete can be appropriate instead. Verify the result, including linked identities, sessions, email metadata and recovery paths.
8. Confirm that the account cannot authenticate or read its former households, private data is gone, and only documented lawful retained records remain. Track provider backup/security-log expiry without restoring deleted identities to active use after a backup restore.

Use the current [Supabase Auth administration documentation](https://supabase.com/docs/reference/javascript/auth-admin-deleteuser) for exact API arguments. The app does not currently automate these operations; implement and test a dedicated privileged workflow before advertising instant deletion.

## Respond and retain minimally

Confirm completion, explain the categories/reason/duration of any retained data, and provide a contact for follow-up. Keep only the minimum support record needed to show fulfilment or meet a lawful obligation. Do not retain an unnecessary full export of the deleted account.

Analytics is deliberately not linked to an account UUID. Tell the requester how to withdraw consent on each installation, and use the provider's supported rights-request tools where historical analytics can be identified. Do not promise that deleting a Supabase account automatically erases Firebase history.

## Payment details

`public.payment_handles` holds a person's optional MB Way phone number and Revolut and PayPal
usernames. Delete the person's row as part of account deletion (it is personal data and is not
needed for financial history). Leaving a household already stops sharing it.
