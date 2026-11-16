package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ResolveExpenseSplitUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseSplitViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.SplitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ExpenseSplitViewModel(
    private val params: ExpenseSplitViewModelContract.Params,
    private val resolveExpenseSplitUseCase: ResolveExpenseSplitUseCase,
) : ExpenseSplitViewModelContract() {
    private data class InnerInputState(
        val method: ExpenseSplitMethodDomain,
        val selected: Set<String>,
        val custom: Map<String, String>,
    )

    private val inputState =
        MutableStateFlow(
            InnerInputState(
                method = params.selection.method,
                selected = params.selection.participants,
                custom =
                    params.selection.values.mapValues { (_, value) ->
                        if (params.selection.method == ExpenseSplitMethodDomain.AMOUNTS)
                            MoneyDomain(value, params.currency).inputText()
                        else value.toString()
                    },
            ),
        )
    private val mutableState =
        MutableStateFlow(
                SplitState(
                    total = MoneyDomain(params.amountMinor, params.currency).formatted(),
                    members = params.members,
                    currency = params.currency,
                ),
            )
            .observeWith(viewModelScope) { state ->
                inputState
                    .map { input ->
                        input to
                            resolveExpenseSplitUseCase(
                                amountMinor = params.amountMinor,
                                currency = params.currency,
                                method = input.method,
                                participants = input.selected,
                                custom = input.custom,
                            )
                    }
                    .updateState(state) { (input, result) ->
                        val selection = (result as? AppResult.Success)?.value
                        val amounts =
                            selection
                                ?.let {
                                    ExpenseSplitDomain.resolve(
                                        amountMinor = params.amountMinor,
                                        selection = it,
                                    )
                                }
                                .orEmpty()
                        copy(
                            method = input.method,
                            selected = input.selected,
                            custom = input.custom,
                            selection = selection,
                            amounts =
                                amounts.mapValues {
                                    MoneyDomain(it.value, params.currency).formatted()
                                },
                            error = (result as? AppResult.Failure)?.error?.toExpensesTextResource(),
                        )
                    }
            }
    override val state = mutableState.asStateFlow()

    override fun onMethodChanged(method: ExpenseSplitMethodDomain) {
        val members = inputState.value.selected.toList()
        val defaults =
            when (method) {
                ExpenseSplitMethodDomain.EQUAL -> emptyMap()
                ExpenseSplitMethodDomain.SHARES -> members.associateWith { 1L }
                ExpenseSplitMethodDomain.AMOUNTS ->
                    if (members.isEmpty()) emptyMap()
                    else
                        ExpenseSplitDomain.equal(
                            amountMinor = params.amountMinor,
                            memberIds = members,
                        )
                ExpenseSplitMethodDomain.PERCENTAGES ->
                    if (members.isEmpty()) emptyMap()
                    else ExpenseSplitDomain.equal(amountMinor = 100, memberIds = members)
            }
        inputState.update { oldState ->
            oldState.copy(
                method = method,
                custom =
                    defaults.mapValues {
                        if (method == ExpenseSplitMethodDomain.AMOUNTS)
                            MoneyDomain(it.value, params.currency).inputText()
                        else it.value.toString()
                    },
            )
        }
    }

    override fun onParticipantToggled(id: String) {
        inputState.update { oldState ->
            oldState.copy(
                selected =
                    if (id in oldState.selected) oldState.selected - id else oldState.selected + id,
                custom =
                    if (id in oldState.custom) oldState.custom
                    else
                        oldState.custom +
                            (id to
                                if (oldState.method == ExpenseSplitMethodDomain.SHARES) "1"
                                else "0"),
            )
        }
    }

    override fun onCustomChanged(id: String, value: String) {
        inputState.update { oldState -> oldState.copy(custom = oldState.custom + (id to value)) }
    }

    override fun onCustomStepped(id: String, increase: Boolean) {
        inputState.update { oldState ->
            oldState.copy(
                custom =
                    oldState.custom +
                        (id to
                            oldState.method.steppedInput(
                                value = oldState.custom[id].orEmpty(),
                                currency = params.currency,
                                increase = increase,
                            )),
            )
        }
    }

    override fun onConfirm() {
        val selection = state.value.selection ?: return
        mutableState.update { oldState -> oldState.copy(confirmed = selection) }
    }

    override fun onNavigationHandled() {
        mutableState.update { oldState -> oldState.copy(confirmed = null) }
    }
}
