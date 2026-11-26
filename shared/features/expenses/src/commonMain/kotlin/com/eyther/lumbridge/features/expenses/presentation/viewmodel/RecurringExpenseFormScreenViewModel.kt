package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormScreenViewModelContract.RedirectTo
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.datetime.LocalDate
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A schedule draft is independent of generated expenses and transient split/date pickers. */
class RecurringExpenseFormScreenViewModel(
    private val params: RecurringExpenseFormScreenViewModelContract.Params,
    private val getRecurringExpenseFormUseCase: GetRecurringExpenseFormUseCase,
    private val saveRecurringExpenseUseCase: SaveRecurringExpenseUseCase,
    private val context: HouseholdContextDomain,
) : RecurringExpenseFormScreenViewModelContract() {
    private data class InnerInputState(
        val title: String = "",
        val amount: String = "",
        val paidBy: String = "",
        val nextOn: LocalDate? = null,
        val split: ExpenseSplitSelectionDomain =
            ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.AMOUNTS, emptySet()),
    )

    private var initialized: Boolean = false
    private val inputState = MutableStateFlow(InnerInputState())
    private val mutableState =
        MutableStateFlow(RecurringExpenseFormState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        title = input.title,
                        amount = input.amount,
                        paidBy = input.paidBy,
                        nextOn = input.nextOn,
                        split = input.split,
                    )
                }
            }
            .launchWith(viewModelScope) { state -> loadForm(state = state) }
    override val state = mutableState.asStateFlow()

    override fun onTitleChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(title = value) }
    }

    override fun onAmountChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(amount = value) }
    }

    override fun onPayerChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(paidBy = value) }
    }

    override fun onDateChanged(value: LocalDate) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(nextOn = value) }
    }

    override fun onSplitConfirmed(value: ExpenseSplitSelectionDomain) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(split = value) }
    }

    override fun onSave() {
        if (!state.value.canSave) return
        val rule = state.value.rule ?: return
        val currency = state.value.currency
        val input = inputState.value
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = safeCall {
                val amount = MoneyDomain.parse(text = input.amount, currency = currency).minorUnits
                saveRecurringExpenseUseCase(
                        rule =
                            rule.copy(
                                title = input.title,
                                amountMinor = amount,
                                paidBy = input.paidBy,
                                nextOn = input.nextOn ?: throw RecurringExpenseError.InvalidDate(),
                                shares =
                                    ExpenseSplitDomain.resolve(
                                        amountMinor = amount,
                                        selection = input.split,
                                    ),
                                splitMethod = input.split.method,
                                splitValues = input.split.values,
                            ),
                    )
                    .getOrThrow()
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, redirectTo = RedirectTo.Saved)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toExpensesTextResource())
                }
            }
        }
    }

    override fun onRetry() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { oldState -> oldState.copy(loading = true, error = null) }
        launch { loadForm(state = mutableState) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadForm(state: MutableStateFlow<RecurringExpenseFormState>) {
        when (
            val result = delayAtLeast {
                getRecurringExpenseFormUseCase(id = params.ruleId, accountId = params.accountId)
            }
        ) {
            is AppResult.Success -> {
                val data = result.value
                if (!initialized) {
                    inputState.value =
                        InnerInputState(
                            title = data.rule.title,
                            amount =
                                MoneyDomain(
                                        minorUnits = data.rule.amountMinor,
                                        currency = data.account.currency,
                                    )
                                    .inputText(),
                            paidBy = data.rule.paidBy,
                            nextOn = data.rule.nextOn,
                            split = data.rule.split,
                        )
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        rule = data.rule,
                        actorIsOwner = data.actorIsOwner,
                        account = data.account,
                        members = data.members.toImmutableList(),
                        loading = false,
                        error = null,
                    )
                }
            }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
                }
        }
    }

    override fun onNavigationHandled() {
        mutableState.update { oldState -> oldState.copy(redirectTo = null) }
    }
}
