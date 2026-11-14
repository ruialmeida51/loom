package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpensesScreenViewModel(
    private val getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    private val context: HouseholdContextDomain,
    private val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
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

    private val inputState = MutableStateFlow(
        InnerInputState(
            paidBy = context.userId,
            from = context.userId,
            occurredOn = todayIso(context.timeZone),
        ),
    )

    private val mutableState = MutableStateFlow(ExpensesState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(
                    title = input.title,
                    amount = input.amount,
                    occurredOn = input.occurredOn,
                    paidBy = input.paidBy,
                    category = input.category,
                    selected = input.selected,
                    splitMode = input.splitMode,
                    custom = input.custom,
                    recurring = input.recurring,
                    from = input.from,
                    to = input.to,
                    settleAmount = input.settleAmount,
                )
            }
        }
        .observeWith(viewModelScope) { state ->
            getExpenseLedgerStreamUseCase().updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(
                        expenses = result.value.expenses,
                        settlements = result.value.settlements,
                        balances = result.value.balances,
                        error = null,
                    )

                    is AppResult.Failure -> copy(error = result.error.toExpensesTextResource())
                }
            }
        }
        .launchWith(viewModelScope) { state ->
            state.loadMembers()
        }

    override val state = mutableState.asStateFlow()

    override fun onTitleChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(title = value)
        }
    }

    override fun onDateChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(occurredOn = value)
        }
    }

    override fun onAmountChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(amount = value)
        }
    }

    override fun onPayerChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(paidBy = value)
        }
    }

    override fun onCategoryChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(category = value)
        }
    }

    override fun onParticipantToggled(id: String) {
        inputState.update { oldState ->
            oldState.copy(
                selected = if (id in oldState.selected) oldState.selected - id else oldState.selected + id,
            )
        }
    }

    override fun onSplitModeChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(splitMode = value)
        }
    }

    override fun onCustomShareChanged(id: String, value: String) {
        inputState.update { oldState ->
            oldState.copy(custom = oldState.custom + (id to value))
        }
    }

    override fun onRecurringChanged(value: Boolean) {
        inputState.update { oldState ->
            oldState.copy(recurring = value)
        }
    }

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (value) {
            expenseId = newId()
            inputState.value = InnerInputState(
                paidBy = context.userId,
                occurredOn = todayIso(context.timeZone),
                from = context.userId,
                selected = state.value.members.map { it.id }.toSet(),
                to = state.value.members.firstOrNull { it.id != context.userId }?.id.orEmpty(),
            )
        }

        mutableState.update { oldState ->
            oldState.copy(showAdd = value, error = null)
        }
    }

    override fun onSettlementSheetVisibilityChanged(value: Boolean) {
        if (value) {
            settlementId = newId()
            val recipientId = state.value.members.firstOrNull { it.id != context.userId }?.id.orEmpty()

            inputState.update { oldState ->
                oldState.copy(
                    settleAmount = "",
                    from = context.userId,
                    to = recipientId,
                )
            }
        }

        mutableState.update { oldState ->
            oldState.copy(showSettle = value, error = null)
        }
    }

    override fun onFromMemberChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(from = value)
        }
    }

    override fun onToMemberChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(to = value)
        }
    }

    override fun onSettlementAmountChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(settleAmount = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onSaveExpense() {
        if (state.value.busy) return
        val expense = try {
            createExpense(inputState.value)
        } catch (error: AppError) {
            mutableState.update { oldState ->
                oldState.copy(error = error.toExpensesTextResource())
            }
            return
        }

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = addExpenseUseCase(expense)
            if (result is AppResult.Success) {
                trackAnalyticsEventUseCase(
                    event = AnalyticsEventDomain.ActionCompleted(
                        action = AnalyticsActionDomain.EXPENSE_RECORDED,
                    ),
                )
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val expenses = oldState.expenses.filterNot { it.id == expense.id } + expense

                        oldState.copy(
                            expenses = expenses,
                            balances = HouseholdLedgerDomain.balances(expenses, oldState.settlements),
                            showAdd = false,
                            busy = false,
                        )
                    }

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toExpensesTextResource(),
                    )
                }
            }
        }
    }

    override fun onRecordSettlement() {
        if (state.value.busy) return
        val input = inputState.value
        val settlement = try {
            SettlementDomain(
                id = settlementId,
                from = input.from,
                to = input.to,
                amountMinor = MoneyDomain.parse(input.settleAmount, context.currency).minorUnits,
                occurredOn = todayIso(context.timeZone),
            )
        } catch (error: AppError) {
            mutableState.update { oldState ->
                oldState.copy(error = error.toExpensesTextResource())
            }
            return
        }

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = recordSettlementUseCase(settlement)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val settlements = oldState.settlements.filterNot { it.id == settlement.id } + settlement

                        oldState.copy(
                            settlements = settlements,
                            balances = HouseholdLedgerDomain.balances(oldState.expenses, settlements),
                            showSettle = false,
                            busy = false,
                        )
                    }

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toExpensesTextResource(),
                    )
                }
            }
        }
    }

    private fun createExpense(input: InnerInputState): ExpenseDomain {
        val amount = MoneyDomain.parse(input.amount, context.currency).minorUnits
        val shares = when (input.splitMode) {
            "Amounts" -> input.selected.associateWith { id ->
                MoneyDomain.parseShare(input.custom[id].orEmpty(), context.currency).minorUnits
            }

            "Percent" -> ExpenseSplitDomain.percentages(
                amountMinor = amount,
                percentages = input.selected.associateWith { input.custom[it]?.toIntOrNull() ?: 0 },
            )

            else -> ExpenseSplitDomain.equal(amountMinor = amount, memberIds = input.selected.toList())
        }

        return ExpenseDomain(
            id = expenseId,
            title = input.title,
            amountMinor = amount,
            paidBy = input.paidBy,
            category = input.category,
            occurredOn = input.occurredOn,
            shares = shares,
            recurring = input.recurring,
        )
    }

    private suspend fun MutableStateFlow<ExpensesState>.loadMembers() {
        when (val result = getMembersUseCase(context.id)) {
            is AppResult.Success -> {
                inputState.update { oldState ->
                    oldState.copy(
                        selected = result.value.map { it.id }.toSet(),
                        to = result.value.firstOrNull { it.id != context.userId }?.id.orEmpty(),
                    )
                }
                update { oldState ->
                    oldState.copy(members = result.value)
                }
            }

            is AppResult.Failure -> update { oldState ->
                oldState.copy(error = result.error.toHouseholdTextResource())
            }
        }
    }
}
