package com.eyther.lumbridge.features.insights.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.generated.resources.Res
import com.eyther.lumbridge.features.insights.generated.resources.statement_saved
import com.eyther.lumbridge.features.insights.presentation.mapper.toInsightsTextResource
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class InsightsScreenViewModel(
    private val getMonthlyStatementStreamUseCase: GetMonthlyStatementStreamUseCase,
    private val saveStatementUseCase: SaveStatementUseCase,
    private val context: HouseholdContextDomain,
    private val exportStatementUseCase: ExportStatementUseCase,
) : InsightsScreenViewModelContract() {

    private val selectedMonth = MutableStateFlow(todayIso(context.timeZone).take(7))

    private val mutableState = MutableStateFlow(InsightsState())
        .observeWith(viewModelScope) { state ->
            getMonthlyStatementStreamUseCase(selectedMonth).updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(
                        statement = result.value,
                        month = result.value.month,
                        loadError = null,
                    )

                    is AppResult.Failure -> copy(loadError = result.error.toInsightsTextResource())
                }
            }
        }

    override val state = mutableState.asStateFlow()

    override fun onPreviousMonth() {
        changeMonth(offset = -1)
    }

    override fun onNextMonth() {
        changeMonth(offset = 1)
    }

    override fun onDismissMessage() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, message = null)
        }
    }

    override fun onSaveStatement() {
        if (state.value.busy) return
        val month = state.value.month

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = saveStatementUseCase(month)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        message = TextResource.Resource(Res.string.statement_saved),
                    )

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toInsightsTextResource(),
                    )
                }
            }
        }
    }

    /** Snapshots the displayed month before launching export; the UI supplies only localized labels. */
    override fun onExportStatement(summaryHeader: String, expenseHeader: String, title: String) {
        if (state.value.busy) return
        val statement = state.value.statement ?: return
        val content = statement.csv(
            currency = context.currency,
            userId = context.userId,
            summaryHeader = summaryHeader,
            expenseHeader = expenseHeader,
        )

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = exportStatementUseCase(
                filename = "loom-${statement.month}.csv",
                content = content,
                title = title,
            )

            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    error = (result as? AppResult.Failure)?.error?.toInsightsTextResource(),
                )
            }
        }
    }

    private fun changeMonth(offset: Int) {
        val date = LocalDate.parse("${selectedMonth.value}-01")
        selectedMonth.value = date.plus(DatePeriod(months = offset)).toString().take(7)
    }
}
