package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveExpensesUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveSettlementsUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.flow.*

class ExpensesScreenViewModel(
    observeExpenses: ObserveExpensesUseCase,
    observeSettlements: ObserveSettlementsUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    private val context: HouseholdContextDomain,
) : ExpensesScreenViewModelContract() {
    private data class InnerInputState(
        val title: String = "",
        val amount: String = "",
        val occurredOn: String = "",
        val paidBy: String = "",
        val category: String = "Groceries",
        val selected: Set<String> = emptySet(),
        val splitMode: String = "Equal",
        val custom: Map<String, String> = emptyMap(),
        val recurring: Boolean = false,
        val from: String = "",
        val to: String = "",
        val settleAmount: String = "",
    )

    private var expenseId = newId()
    private var settlementId = newId()
    private val inputState =
        MutableStateFlow(
            InnerInputState(
                paidBy = context.userId,
                from = context.userId,
                occurredOn = todayIso(context.timeZone),
            ),
        )

    private val mutableState =
        MutableStateFlow(ExpensesState(paidBy = context.userId, from = context.userId)).observeWith(
            viewModelScope,
        ) { state ->
            inputState.updateState(state) {
                copy(
                    title = it.title,
                    amount = it.amount,
                    occurredOn = it.occurredOn,
                    paidBy = it.paidBy,
                    category = it.category,
                    selected = it.selected,
                    splitMode = it.splitMode,
                    custom = it.custom,
                    recurring = it.recurring,
                    from = it.from,
                    to = it.to,
                    settleAmount = it.settleAmount,
                )
            }
        }
    override val state = mutableState.asStateFlow()

    init {
        launch {
            getMembersUseCase(context.id).let { result ->
                when (result) {
                    is AppResult.Success -> {
                        inputState.update {
                            it.copy(
                                selected = result.value.map { member -> member.id }.toSet(),
                                to =
                                    result.value
                                        .firstOrNull { member -> member.id != context.userId }
                                        ?.id
                                        .orEmpty(),
                            )
                        }
                        mutableState.update { it.copy(members = result.value) }
                    }
                    is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
                }
            }
        }
        mutableState.observeWith(viewModelScope) {
            combine(observeExpenses(), observeSettlements()) { expenses, settlements ->
                    Triple(
                        expenses,
                        settlements,
                        HouseholdLedgerDomain.balances(expenses, settlements),
                    )
                }
                .catch { error -> mutableState.update { it.copy(error = error.message) } }
                .onEach { (expenses, settlements, balances) ->
                    mutableState.update {
                        it.copy(expenses = expenses, settlements = settlements, balances = balances)
                    }
                }
        }
    }

    override fun title(value: String) {
        inputState.update { it.copy(title = value) }
    }

    override fun occurredOn(value: String) {
        inputState.update { it.copy(occurredOn = value) }
    }

    override fun amount(value: String) {
        inputState.update { it.copy(amount = value) }
    }

    override fun payer(value: String) {
        inputState.update { it.copy(paidBy = value) }
    }

    override fun category(value: String) {
        inputState.update { it.copy(category = value) }
    }

    override fun selected(id: String) {
        inputState.update {
            it.copy(selected = if (id in it.selected) it.selected - id else it.selected + id)
        }
    }

    override fun splitMode(value: String) {
        inputState.update { it.copy(splitMode = value) }
    }

    override fun custom(id: String, value: String) {
        inputState.update { it.copy(custom = it.custom + (id to value)) }
    }

    override fun recurring(value: Boolean) {
        inputState.update { it.copy(recurring = value) }
    }

    override fun showAdd(value: Boolean) {
        if (value) {
            expenseId = newId()
            inputState.value =
                InnerInputState(
                    paidBy = context.userId,
                    occurredOn = todayIso(context.timeZone),
                    from = context.userId,
                    selected = mutableState.value.members.map { it.id }.toSet(),
                    to =
                        mutableState.value.members
                            .firstOrNull { it.id != context.userId }
                            ?.id
                            .orEmpty(),
                )
        }
        mutableState.update { it.copy(showAdd = value, error = null) }
    }

    override fun showSettle(value: Boolean) {
        if (value) {
            settlementId = newId()
            inputState.update {
                it.copy(
                    settleAmount = "",
                    from = context.userId,
                    to =
                        mutableState.value.members
                            .firstOrNull { it.id != context.userId }
                            ?.id
                            .orEmpty(),
                )
            }
        }
        mutableState.update { it.copy(showSettle = value, error = null) }
    }

    override fun from(value: String) {
        inputState.update { it.copy(from = value) }
    }

    override fun to(value: String) {
        inputState.update { it.copy(to = value) }
    }

    override fun settleAmount(value: String) {
        inputState.update { it.copy(settleAmount = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun add() {
        if (mutableState.value.busy) return
        val input = inputState.value
        val expense =
            try {
                val amount = MoneyDomain.parse(input.amount, context.currency).minorUnits
                val shares =
                    when (input.splitMode) {
                        "Amounts" ->
                            input.selected.associateWith { id ->
                                val value = input.custom[id].orEmpty()
                                if (value.trim() in listOf("0", "0.00", "0,00")) 0L
                                else MoneyDomain.parse(value, context.currency).minorUnits
                            }
                        "Percent" ->
                            ExpenseSplitDomain.percentages(
                                amount,
                                input.selected.associateWith {
                                    input.custom[it]?.toIntOrNull() ?: 0
                                },
                            )
                        else -> ExpenseSplitDomain.equal(amount, input.selected.toList())
                    }
                ExpenseDomain(
                    expenseId,
                    input.title,
                    amount,
                    input.paidBy,
                    input.category,
                    input.occurredOn,
                    shares,
                    input.recurring,
                )
            } catch (error: IllegalArgumentException) {
                mutableState.update { it.copy(error = error.message) }
                return
            }
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = addExpenseUseCase(expense)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        val expenses = old.expenses.filterNot { it.id == expense.id } + expense
                        old.copy(
                            expenses = expenses,
                            balances = HouseholdLedgerDomain.balances(expenses, old.settlements),
                            title = "",
                            amount = "",
                            showAdd = false,
                            busy = false,
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(error = result.message, busy = false) }
            }
        }
    }

    override fun settle() {
        if (mutableState.value.busy) return
        val input = inputState.value
        val settlement =
            try {
                SettlementDomain(
                    settlementId,
                    input.from,
                    input.to,
                    MoneyDomain.parse(input.settleAmount, context.currency).minorUnits,
                    todayIso(context.timeZone),
                )
            } catch (error: IllegalArgumentException) {
                mutableState.update { it.copy(error = error.message) }
                return
            }
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = recordSettlementUseCase(settlement)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        val settlements =
                            old.settlements.filterNot { it.id == settlement.id } + settlement
                        old.copy(
                            settlements = settlements,
                            balances = HouseholdLedgerDomain.balances(old.expenses, settlements),
                            showSettle = false,
                            busy = false,
                            settleAmount = "",
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }
}
