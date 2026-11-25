# Guests on trip and event accounts

Status: approved, implemented · Target: 4.0.18 · Gate: `account_guests` (on for everyone at launch)

## Problem

Money is shared outside the household: a trip with a friend's partner, a gift with a colleague,
a dinner with neighbours. Today everyone on an expense must be a household member, which would give
them the household's shopping, tasks and everyday money. People fall back to a second app or to
mental maths.

## Goal

A trip or event account (never Everyday) can include **guests**: named people without a Loom
account who can pay, be in a split and settle up, inside that one account only. Household members
record everything for them.

Out of scope for this release (planned follow-ups, see the end): a share link for guests, a guest
claiming their profile after installing Loom, and multi-currency.

## Experience

### Adding guests

- **Account form (create or edit a trip/event).** Below "Who's in", household members stay as
  checkboxes, followed by guests as rows and **Add a guest**. Adding asks for a name only
  (1–40 characters) and adds it to the list at once. A guest row's ⋮ menu offers Rename and
  Remove.
- **Everyday** shows no guest section. Its member list keeps following the household.
- **Limit:** up to 20 guests per account (a sanity bound, not a plan limit).

### Everywhere guests appear

| Place | Guest treatment |
| --- | --- |
| Expense form "Paid by" and the split sheet | Listed after members with a **Guest** tag; same split methods |
| Ledger rows ("Paid by Marta · …") | Name as entered |
| Expense details shares | Name + Guest tag; avatar uses initials in the guest's stable colour (no photo) |
| Balances, "Who owes whom", Everyone's balance | Same as members, with the tag |
| Record payment | Either side can be a guest; any account member records it |
| Home balance card | Unchanged: it reads Everyday, which has no guests |
| Insights / personal statements | Count only the viewer's own share, so guests change nothing |
| Push notifications | Guests receive none; members are notified as today |

### Removing a guest

- A guest with a **zero balance** can be removed. Their past expenses and payments stay and keep
  showing the name, like a former member.
- A guest with a **non-zero balance** cannot be removed. The confirmation explains why and offers
  **Record payment** to settle first, the same rule as leaving a household.
- Renaming updates every record, because records store the guest's id, not the name.

### Archiving and deleting

- Archiving an account with guests works as today, with no settle-up required (archived accounts
  are read-only and keep their balances visible).
- Deleting the household deletes guests with the account data.

## Rules (server-enforced)

1. Guests belong to exactly one account and one household; their ids never appear in another
   account's records.
2. Only trip/event accounts (`not is_default`) accept guests.
3. Only active household members on the account can add, rename or remove guests, and record
   expenses or payments involving them. Guests themselves never authenticate.
4. A participant in `paid_by`, `expense_shares.user_id`, `settlements.from_user/to_user` and
   recurring rules must be an active account member **or** an active guest of that account
   (removed guests remain valid on records that already include them, like former members).
5. A guest with a non-zero account balance cannot be removed.
6. Recurring bills may include guests (useful for a shared subscription); a removed guest stops
   recurring rules from generating, and the rule is paused with a visible reason.

## Data model

New table, one migration:

```sql
create table public.expense_account_guests (
    id uuid primary key,                       -- client-generated, like other records
    account_id uuid not null,
    household_id uuid not null,
    name text not null check (length(trim(name)) between 1 and 40),
    removed_at timestamptz,
    version bigint not null default 1,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),
    foreign key (account_id, household_id) references public.expense_accounts(id, household_id)
);
```

Participants today are foreign keys to `household_members(household_id, user_id)` on
`expenses.paid_by`, `expense_shares.user_id`, `settlements.from_user/to_user` and
`recurring_expenses.paid_by`. Guests are not household members, so the migration:

- Replaces those four foreign keys with one validation, `loom_private.is_account_participant(account, id)`,
  used by the existing save functions and by a constraint trigger on each table as a backstop.
- Extends `validate_retained_account_expense` and the settlement validation to accept guests
  under rule 4.
- Adds RPCs: `save_account_guest(p_household_id, p_account_id, p_id, p_name, p_version)` (create
  or rename) and `remove_account_guest(p_household_id, p_account_id, p_id, p_version)`
  (enforces rule 5).
- Reads: guests come with the account, through `expense_accounts_with_members` gaining a `guests`
  JSON column (appended, so older clients ignore it). RLS: members of the household can read the
  guests of accounts they belong to.

Guest ids are random UUIDs that never collide with user ids, so ledgers keep keying balances by
one id space. `HouseholdLedgerDomain` needs no change.

## Client

- **Domain:** `ExpenseGuestDomain(id, name, removed)`. `ExpenseAccountDomain` gains `guests`.
  A presentation-level participant model (`AccountParticipantUi`: id, name, avatarPath,
  isGuest, removed) replaces raw `MemberDomain` lists wherever money UI names people, built from
  members + guests in one mapper so every screen agrees.
- **Data:** guest response/request models and `saveGuest`/`removeGuest` on
  `ExpenseAccountDataSource`, through `access.write`.
- **Use cases:** `SaveAccountGuestUseCase`, `RemoveAccountGuestUseCase` (name trimming and length
  check in domain; balance rule on the server, mapped to `ExpenseAccountError.GuestHasBalance`).
- **UI:** account form guest section, guest rename/remove sheet, Guest tags in pickers, rows and
  balances; strings in the expenses module.
- **Demo:** the trip account in the demo household gets one guest with a small balance, so the
  preview and UI tests exercise it.

## Gating groundwork (shipped with this feature)

- `public.household_features(household_id, feature, enabled)` with no rows meaning "on"; read
  through `loom_private.feature_enabled(household, feature)`.
- Client `FeatureGate` port in core (`isEnabled(feature): Boolean`, stream-backed, defaults to
  on), provided per household graph; screens ask it through their ViewModel state.
- `account_guests` is the first feature: when off, the guest section is hidden and the server
  rejects new guests, while existing guests and their records stay readable and settleable.
  Tests cover both states.

## Compatibility

- **Older clients (4.0.17 and earlier)** read accounts and ledgers that contain guest ids they
  cannot name: they show "Former member", and balances stay correct. Editing an expense that
  includes a guest from an old client keeps the guest, because validation accepts existing
  participants. We release the client together with the migration and keep this degradation
  documented.
- No existing data changes; the foreign-key replacement is behaviour-preserving for members.

## Tests

- **Database:** guests only on trip accounts; cross-account guest ids rejected; non-members
  rejected; guest payer and shares; guest↔member settlement; removal blocked with balance and
  allowed at zero; renamed guest; removed guest still valid on old records; feature switched off
  blocks new guests only; foreign-key replacement still rejects unknown ids.
- **Domain/ViewModel:** participants merge and ordering; guest name validation; removal error
  mapping; form adds/renames/removes guests in its draft.
- **UI (demo):** add a guest on a trip, split an expense with them, see "Marta owes you", record
  their payment, remove them once settled.

## Follow-ups (not in this release)

1. **Share link:** a read-only page of a guest's balance and the account's expenses, opened from a
   link.
2. **Claim:** a guest who installs Loom accepts an invite to that account and their guest id is
   merged into their user id across the account's records.
3. **Multi-currency trips:** per-expense currency with a stored rate, building on guests.

## Open questions

1. Should guests also be allowed on **Everyday**? (Proposed: no, to keep Everyday "the household".)
2. Should recurring bills accept guests, or only one-off expenses? (Proposed: yes, as above.)
3. Guest **limit** per account: 20 as a sanity bound? (Not a monetisation limit.)
