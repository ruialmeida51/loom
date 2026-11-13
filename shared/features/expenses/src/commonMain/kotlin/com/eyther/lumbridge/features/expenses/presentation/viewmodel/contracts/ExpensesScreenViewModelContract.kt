package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.*

data class ExpensesState(
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
    val error: String? = null,
)

abstract class ExpensesScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<ExpensesState>

    abstract fun title(value: String)

    abstract fun occurredOn(value: String)

    abstract fun amount(value: String)

    abstract fun payer(value: String)

    abstract fun category(value: String)

    abstract fun selected(id: String)

    abstract fun splitMode(value: String)

    abstract fun custom(id: String, value: String)

    abstract fun recurring(value: Boolean)

    abstract fun showAdd(value: Boolean)

    abstract fun showSettle(value: Boolean)

    abstract fun from(value: String)

    abstract fun to(value: String)

    abstract fun settleAmount(value: String)

    abstract fun add()

    abstract fun settle()

    abstract fun dismiss()
}
