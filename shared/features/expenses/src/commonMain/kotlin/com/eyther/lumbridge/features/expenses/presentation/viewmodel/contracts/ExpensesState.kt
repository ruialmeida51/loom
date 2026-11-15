package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistorySectionUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

data class ExpensesState(
    val userId: String = "",
    val account: ExpenseAccountDomain? = null,
    val accounts: List<ExpenseAccountDomain> = emptyList(),
    val expenses: List<ExpenseDomain> = emptyList(),
    val settlements: List<SettlementDomain> = emptyList(),
    val members: List<MemberDomain> = emptyList(),
    val balances: Map<String, Long> = emptyMap(),
    val selectedView: ExpensesViewUi = ExpensesViewUi.EXPENSES,
    val showAccounts: Boolean = false,
    val showManageAccounts: Boolean = false,
    val deletingAccount: ExpenseAccountDomain? = null,
    val showSettle: Boolean = false,
    val from: String = "",
    val to: String = "",
    val settleAmount: String = "",
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {
    val currency: String = account?.currency ?: "EUR"
    val role: String? = members.firstOrNull { it.id == userId }?.role
    val isOwner: Boolean = role == "owner"
    val accountMembers: List<MemberDomain> = members.filter {
        it.id in account?.memberIds.orEmpty()
    }
    val canCreateExpense: Boolean =
        account?.canAddExpense(userId = userId) == true &&
            accountMembers.isNotEmpty() &&
            !busy &&
            !loading
    val canRecordSettlement: Boolean =
        userId in account?.memberIds.orEmpty() && accountMembers.size > 1 && !busy && !loading
    val canManageAccount: Boolean = account?.canManage(userId = userId, isOwner = isOwner) == true
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
    val displayError: TextResource? = error ?: loadError
    val activeAccounts: List<ExpenseAccountDomain> =
        accounts
            .filterNot { it.archived }
            .sortedWith(
                compareByDescending<ExpenseAccountDomain> { it.isDefault }
                    .thenBy { it.name.lowercase() },
            )
    val archivedAccounts: List<ExpenseAccountDomain> =
        accounts.filter { it.archived }.sortedBy { it.name.lowercase() }
    val memberNames: ImmutableMap<String, String> =
        members.associate { it.id to it.name }.toImmutableMap()
    val suggestions: ImmutableList<SuggestedSettlementDomain> =
        HouseholdLedgerDomain.suggestions(balances).toImmutableList()
    val ownBalance: BalanceSummaryUi =
        BalanceSummaryUi(
            money = MoneyDomain(minorUnits = balances[userId] ?: 0L, currency = currency),
        )
    val memberBalances: ImmutableList<MemberBalanceUi> =
        (account?.memberIds.orEmpty() + balances.keys)
            .map { id ->
                MemberBalanceUi(
                    member =
                        members.firstOrNull { it.id == id }
                            ?: MemberDomain(id = id, name = "", role = ""),
                    balance =
                        BalanceSummaryUi(
                            money =
                                MoneyDomain(minorUnits = balances[id] ?: 0L, currency = currency),
                        ),
                )
            }
            .toImmutableList()
    val history: ImmutableList<ExpenseHistorySectionUi> =
        (expenses.map {
                ExpenseHistoryItemUi.Purchase(
                    expense = it,
                    payerName = memberNames[it.paidBy],
                    amount = MoneyDomain(it.amountMinor, currency).formatted(),
                )
            } +
                settlements.map {
                    ExpenseHistoryItemUi.Repayment(
                        settlement = it,
                        fromName = memberNames[it.from],
                        toName = memberNames[it.to],
                        amount = MoneyDomain(it.amountMinor, currency).formatted(),
                    )
                })
            .sortedWith(compareByDescending<ExpenseHistoryItemUi> { it.date }.thenBy { it.key })
            .groupBy { it.date }
            .map { (date, items) ->
                ExpenseHistorySectionUi(date = date, items = items.toImmutableList())
            }
            .toImmutableList()
}
