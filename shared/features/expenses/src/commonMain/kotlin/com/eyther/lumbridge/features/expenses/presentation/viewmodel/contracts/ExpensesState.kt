package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.components.pagination.PaginationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerRecordDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.history_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.payment_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.payment_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.SuggestedRepaymentUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

data class ExpensesState(
    val userId: String = "",
    val account: ExpenseAccountDomain? = null,
    val accounts: List<ExpenseAccountDomain> = emptyList(),
    val expenses: List<ExpenseDomain> = emptyList(),
    val settlements: List<SettlementDomain> = emptyList(),
    val members: List<MemberDomain> = emptyList(),
    /** Everyone's balance over the account's whole history, as the server adds it up. */
    val balances: Map<String, Long> = emptyMap(),
    /** Older ledger records exist beyond the loaded pages. */
    val hasMoreHistory: Boolean = false,
    val loadingMoreHistory: Boolean = false,
    val loadMoreHistoryError: TextResource? = null,
    val recurring: List<RecurringExpenseDomain> = emptyList(),
    val selectedView: ExpensesViewUi = ExpensesViewUi.EXPENSES,
    val showAccounts: Boolean = false,
    val showMemberBalances: Boolean = false,
    val deletingAccount: ExpenseAccountDomain? = null,
    val deletingExpense: ExpenseDomain? = null,
    val deletingSettlement: SettlementDomain? = null,
    val entryDeleteError: TextResource? = null,
    val successMessage: TextResource? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {
    val currency: String = account?.currency ?: "EUR"
    val role: String? = members.firstOrNull { it.id == userId }?.role
    val isOwner: Boolean = members.any { it.id == userId && it.isOwner }
    /** Household members plus this account's guests: everyone balances and names cover. */
    private val participants: List<MemberDomain> = account?.withGuests(members) ?: members
    val accountMembers: List<MemberDomain> = participants.filter {
        it.id in account?.participantIds.orEmpty()
    }
    val canCreateExpense: Boolean =
        account?.canAddExpense(userId = userId) == true &&
            accountMembers.isNotEmpty() &&
            !busy &&
            !loading
    private val settlementAvailable: Boolean =
        userId in account?.memberIds.orEmpty() &&
            account?.archived == false &&
            accountMembers.size > 1
    val canRecordSettlement: Boolean = settlementAvailable && !busy && !loading
    val deleteConfirmation: DestructiveConfirmationUi? = deletingAccount?.let { target ->
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.account_delete_title),
            message =
                TextResource.Resource(
                    Res.string.account_delete_message,
                    args = listOf(target.name),
                ),
            confirmText = TextResource.Resource(Res.string.account_delete),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
    val entryDeleteConfirmation: DestructiveConfirmationUi? =
        when {
            deletingExpense != null ->
                DestructiveConfirmationUi(
                    title = TextResource.Resource(Res.string.detail_delete_title),
                    message =
                        TextResource.Resource(
                            Res.string.history_delete_message,
                            args = listOf(deletingExpense.title),
                        ),
                    confirmText = TextResource.Resource(Res.string.detail_delete),
                    cancelText = TextResource.Resource(Res.string.ui_cancel),
                )
            deletingSettlement != null ->
                DestructiveConfirmationUi(
                    title = TextResource.Resource(Res.string.payment_delete_title),
                    message =
                        TextResource.Resource(
                            Res.string.payment_delete_message,
                            args =
                                listOf(
                                    MoneyDomain(deletingSettlement.amountMinor, currency)
                                        .formatted(),
                                ),
                        ),
                    confirmText = TextResource.Resource(Res.string.detail_delete),
                    cancelText = TextResource.Resource(Res.string.ui_cancel),
                )
            else -> null
        }
    val displayError: TextResource? = error ?: loadError

    /** Only the Expenses view pages; balances and suggestions come whole from the server. */
    val historyPagination: PaginationUi =
        PaginationUi(
            hasMore = hasMoreHistory,
            loadingMore = loadingMoreHistory,
            loadMoreError = loadMoreHistoryError,
            enabled = !loading && loadError == null && selectedView == ExpensesViewUi.EXPENSES,
        )
    val activeAccounts: ImmutableList<ExpenseAccountDomain> =
        accounts
            .filterNot { it.archived }
            .sortedWith(
                compareByDescending<ExpenseAccountDomain> { it.isDefault }
                    .thenBy { it.name.lowercase() },
            )
            .toImmutableList()
    val archivedAccounts: ImmutableList<ExpenseAccountDomain> =
        accounts.filter { it.archived }.sortedBy { it.name.lowercase() }.toImmutableList()
    val manageableAccountIds: ImmutableSet<String> =
        accounts
            .filter {
                it.canManage(userId = userId, isOwner = isOwner)
            }
            .map { it.id }
            .toImmutableSet()
    val memberNames: ImmutableMap<String, String> =
        participants.associate { it.id to it.name }.toImmutableMap()
    val suggestions: ImmutableList<SuggestedSettlementDomain> =
        HouseholdLedgerDomain.suggestions(balances).toImmutableList()
    val suggestedRepayments: ImmutableList<SuggestedRepaymentUi> =
        suggestions
            .map { settlement ->
                SuggestedRepaymentUi(
                    settlement = settlement,
                    fromName = memberNames[settlement.from],
                    toName = memberNames[settlement.to],
                    amount = MoneyDomain(settlement.amountMinor, currency).formatted(),
                    isCurrentUserSender = userId == settlement.from,
                    isCurrentUserRecipient = userId == settlement.to,
                    canRecord =
                        settlementAvailable &&
                            account?.canRecordPayment(userId, settlement.from, settlement.to) ==
                                true,
                    recordEnabled =
                        canRecordSettlement &&
                            account?.canRecordPayment(userId, settlement.from, settlement.to) ==
                                true,
                    counterpartyAvatarPath =
                        participants
                            .firstOrNull {
                                it.id ==
                                    if (userId == settlement.from) settlement.to else settlement.from
                            }
                            ?.avatarPath,
                )
            }
            .toImmutableList()
    val ownBalance: BalanceSummaryUi =
        BalanceSummaryUi(
            money = MoneyDomain(minorUnits = balances[userId] ?: 0L, currency = currency),
        )
    val showPersonalSummary: Boolean =
        suggestedRepayments.count { it.isCurrentUserSender || it.isCurrentUserRecipient } != 1
    /** The shortcut above the ledger: how many bills repeat here and which comes next. */
    val recurringSummary: RecurringSummaryUi? = account?.let {
        RecurringSummaryUi.from(rules = recurring, currency = currency)
    }

    val memberBalances: ImmutableList<MemberBalanceUi> =
        (account?.memberIds.orEmpty() + account?.guests.orEmpty().filterNot { it.removed }.map { it.id } + balances.keys)
            .map { id ->
                MemberBalanceUi(
                    member =
                        participants.firstOrNull { it.id == id }
                            ?: MemberDomain(id = id, name = "", role = ""),
                    balance =
                        BalanceSummaryUi(
                            money =
                                MoneyDomain(minorUnits = balances[id] ?: 0L, currency = currency),
                        ),
                )
            }
            .toImmutableList()
    /** The loaded records, newest entry first in the order people added them. */
    val history: ImmutableList<ExpenseHistoryItemUi> =
        (expenses.map { LedgerRecordDomain.Purchase(expense = it) } +
                settlements.map { LedgerRecordDomain.Repayment(settlement = it) })
            // The same order the server pages in, so a new page only ever adds rows below.
            .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
            .map { record ->
                when (record) {
                    is LedgerRecordDomain.Purchase ->
                        ExpenseHistoryItemUi.Purchase(
                            expense = record.expense,
                            payerName = memberNames[record.expense.paidBy],
                            canManage =
                                account?.canAddExpense(userId = userId) == true &&
                                    record.expense.canManage(userId = userId, isOwner = isOwner),
                            amount = MoneyDomain(record.expense.amountMinor, currency).formatted(),
                            payerAvatarPath =
                                participants
                                    .firstOrNull { member -> member.id == record.expense.paidBy }
                                    ?.avatarPath,
                            originalAmount =
                                record.expense.original?.let { original ->
                                    MoneyDomain(original.amountMinor, original.currency).formatted()
                                },
                        )
                    is LedgerRecordDomain.Repayment ->
                        ExpenseHistoryItemUi.Repayment(
                            settlement = record.settlement,
                            fromName = memberNames[record.settlement.from],
                            toName = memberNames[record.settlement.to],
                            canManage =
                                account?.archived == false &&
                                    record.settlement.canManage(userId = userId, isOwner = isOwner),
                            isUserSender = record.settlement.from == userId,
                            isUserRecipient = record.settlement.to == userId,
                            amount =
                                MoneyDomain(record.settlement.amountMinor, currency).formatted(),
                        )
                }
            }
            .toImmutableList()
}
