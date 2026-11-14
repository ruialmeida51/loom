package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpensesState(
    val userId: String = "",
    val currency: String = "EUR",
    val expenses: List<ExpenseDomain> = emptyList(),
    val settlements: List<SettlementDomain> = emptyList(),
    val members: List<MemberDomain> = emptyList(),
    val balances: Map<String, Long> = emptyMap(),
    val title: String = "",
    val amount: String = "",
    val occurredOn: String = "",
    val paidBy: String = "",
    val category: String = "Groceries",
    val selected: Set<String> = emptySet(),
    val splitMode: String = "Equal",
    val custom: Map<String, String> = emptyMap(),
    val recurring: Boolean = false,
    val showAdd: Boolean = false,
    val showSettle: Boolean = false,
    val from: String = "",
    val to: String = "",
    val settleAmount: String = "",
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError

    val ownBalance: BalanceSummaryUi = BalanceSummaryUi(
        money = MoneyDomain(minorUnits = balances[userId] ?: 0L, currency = currency),
    )
    val memberNames: Map<String, String> = members.associate { it.id to it.name }
    val memberBalances: List<MemberBalanceUi> = members.map { member ->
        MemberBalanceUi(
            member = member,
            balance = BalanceSummaryUi(
                money = MoneyDomain(minorUnits = balances[member.id] ?: 0L, currency = currency),
            ),
        )
    }
}
