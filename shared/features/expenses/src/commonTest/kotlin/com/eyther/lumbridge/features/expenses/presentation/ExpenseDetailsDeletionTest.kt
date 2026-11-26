package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseDetailsDeletionTest {
    @Test
    fun returningFromEditDuringTheInitialReceiptReadStillReloadsFinancialDetails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        val before =
            ExpenseDomain(
                id = "expense",
                title = "Dinner",
                amountMinor = 2000,
                paidBy = "you",
                category = "Other",
                occurredOn = LocalDate.parse("2026-11-18"),
                shares = mapOf("you" to 1000, "alex" to 1000),
                accountId = fixture.account.id,
                version = 3,
                createdBy = "you",
            )
        fixture.loadedExpense = before
        val gate = CompletableDeferred<AppResult<ExpenseReceiptDomain>>()
        fixture.receipts.readGate = gate
        val viewModel = fixture.detailsViewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertEquals(2000, viewModel.state.value.expense?.amountMinor)
            assertTrue(viewModel.state.value.receiptLoading)
            fixture.loadedExpense = before.copy(amountMinor = 3000, version = 4)
            viewModel.onContentChanged()
            fixture.receipts.readGate = null
            gate.complete(
                AppResult.Success(
                    ExpenseReceiptDomain(expenseId = "expense", accountId = fixture.account.id),
                ),
            )
            runCurrent()
            assertEquals(3000, viewModel.state.value.expense?.amountMinor)
            assertFalse(viewModel.state.value.refreshing)
        } finally {
            gate.complete(
                AppResult.Success(
                    ExpenseReceiptDomain(expenseId = "expense", accountId = fixture.account.id),
                ),
            )
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aLateReceiptReadCannotReplaceANewlySavedReceipt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        fixture.loadedExpense =
            ExpenseDomain(
                id = "expense",
                title = "Dinner",
                amountMinor = 2000,
                paidBy = "you",
                category = "Other",
                occurredOn = LocalDate.parse("2026-11-18"),
                shares = mapOf("you" to 1000, "alex" to 1000),
                accountId = fixture.account.id,
                version = 3,
                createdBy = "you",
            )
        val before =
            ExpenseReceiptDomain(
                expenseId = "expense",
                accountId = fixture.account.id,
                objectPath = "old.jpg",
                version = 2,
            )
        fixture.receipts.receipt = before
        val viewModel = fixture.detailsViewModel()
        val gate = CompletableDeferred<AppResult<ExpenseReceiptDomain>>()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            viewModel.onReceiptPicked(byteArrayOf(1, 2, 3))
            runCurrent()
            fixture.receipts.readGate = gate
            viewModel.onReceiptRetry()
            runCurrent()
            assertTrue(viewModel.state.value.receiptLoading)
            viewModel.onReceiptSave()
            runCurrent()
            assertEquals(3, viewModel.state.value.receipt?.version)
            gate.complete(AppResult.Success(before))
            runCurrent()
            assertEquals(3, viewModel.state.value.receipt?.version)
            assertFalse(viewModel.state.value.receiptLoading)
        } finally {
            gate.complete(AppResult.Success(before))
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedDeletionKeepsItsConfirmationErrorAndOriginalVersionForRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        val expense =
            ExpenseDomain(
                id = "expense",
                title = "Dinner",
                amountMinor = 2000,
                paidBy = "you",
                category = "Other",
                occurredOn = LocalDate.parse("2026-11-18"),
                shares = mapOf("you" to 1000, "alex" to 1000),
                accountId = fixture.account.id,
                version = 3,
                createdBy = "you",
            )
        fixture.loadedExpense = expense
        val viewModel = fixture.detailsViewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertTrue(viewModel.state.value.canEdit)
            viewModel.onDeleteRequested()
            fixture.deleteResult = AppResult.Failure(CommonError.NoConnection())
            viewModel.onDeleteConfirmed()
            runCurrent()
            assertNotNull(viewModel.state.value.deleteConfirmation)
            assertNotNull(viewModel.state.value.deleteError)
            assertFalse(viewModel.state.value.busy)
            fixture.loadedExpense = expense.copy(version = 4, title = "Changed by someone else")
            viewModel.onRefresh()
            runCurrent()
            assertFalse(viewModel.state.value.refreshing)
            fixture.deleteResult = AppResult.Success(Unit)
            viewModel.onDeleteConfirmed()
            runCurrent()
            assertEquals(ExpenseDetailsScreenViewModelContract.RedirectTo.Deleted, viewModel.state.value.redirectTo)
            assertEquals(listOf(3L, 3L), fixture.deletedExpenses.map { it.version })
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
