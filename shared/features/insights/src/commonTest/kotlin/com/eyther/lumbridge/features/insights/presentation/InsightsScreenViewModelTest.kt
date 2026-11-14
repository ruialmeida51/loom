package com.eyther.lumbridge.features.insights.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.presentation.viewmodel.InsightsScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsScreenViewModelTest {

    @Test
    fun exportKeepsTheDisplayedStatementWhenTheMonthOrLedgerChangesBeforeSharing() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val context = HouseholdContextDomain(
            id = "household",
            userId = "member",
            name = "Home",
            currency = "EUR",
            timeZone = "UTC",
        )
        val expenses = MutableStateFlow<AppResult<List<ExpenseDomain>>>(AppResult.Success(emptyList()))
        val expenseSource = object : ExpenseDataSource {
            override fun streamExpenses() = expenses

            override fun streamSettlements() = flowOf(AppResult.Success(emptyList<SettlementDomain>()))

            override suspend fun addExpense(expense: ExpenseDomain): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
                AppResult.Success(Unit)
        }
        val statementSource = object : StatementDataSource {
            override suspend fun saveStatement(month: String): AppResult<Unit> = AppResult.Success(Unit)
        }
        val finishSharing = CompletableDeferred<Unit>()
        var exportedFilename: String? = null
        var exportedContent: String? = null
        var exportedTitle: String? = null
        val exportSource = object : StatementExportDataSource {
            override suspend fun export(filename: String, content: String, title: String): AppResult<Unit> {
                exportedFilename = filename
                exportedContent = content
                exportedTitle = title
                finishSharing.await()
                return AppResult.Success(Unit)
            }
        }
        val viewModel = InsightsScreenViewModel(
            getMonthlyStatementStreamUseCase = GetMonthlyStatementStreamUseCase(
                getExpenseLedgerStreamUseCase = GetExpenseLedgerStreamUseCase(
                    getExpensesStreamUseCase = GetExpensesStreamUseCase(expenseDataSource = expenseSource),
                    getSettlementsStreamUseCase = GetSettlementsStreamUseCase(expenseDataSource = expenseSource),
                ),
                context = context,
            ),
            saveStatementUseCase = SaveStatementUseCase(statementDataSource = statementSource),
            context = context,
            exportStatementUseCase = ExportStatementUseCase(statementExportDataSource = exportSource),
        )

        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            val displayedMonth = viewModel.state.value.month
            expenses.value = AppResult.Success(
                listOf(
                    ExpenseDomain(
                        id = "rent",
                        title = "Rent",
                        amountMinor = 10000,
                        paidBy = context.userId,
                        category = "housing",
                        occurredOn = "$displayedMonth-01",
                        shares = mapOf(context.userId to 10000L),
                    ),
                ),
            )
            runCurrent()

            viewModel.onExportStatement(
                summaryHeader = "mes,moeda,total,pago,parte,inicial,final",
                expenseHeader = "data,descricao,categoria,valor,parte",
                title = "Extrato mensal",
            )
            viewModel.onPreviousMonth()
            expenses.value = AppResult.Success(emptyList())
            runCurrent()

            val content = assertNotNull(exportedContent)
            assertEquals("loom-$displayedMonth.csv", exportedFilename)
            assertEquals("Extrato mensal", exportedTitle)
            assertTrue(content.startsWith("mes,moeda,total,pago,parte,inicial,final\r\n"))
            assertTrue(content.contains("data,descricao,categoria,valor,parte\r\n"))
            assertTrue(content.contains("$displayedMonth-01,\"Rent\",\"housing\",10000,10000"))
            assertNotEquals(displayedMonth, viewModel.state.value.month)

            finishSharing.complete(Unit)
            runCurrent()
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
