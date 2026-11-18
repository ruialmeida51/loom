package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseReceiptCaptureTest {
    @Test
    fun aPhotoRetryNeverCreatesOrUpdatesTheFinancialExpenseAgain() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        val store = ViewModelStore()
        val model = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            model.onTitleChanged("Groceries")
            model.onAmountChanged("24.00")
            model.onReceiptPicked(byteArrayOf(1, 2, 3))
            runCurrent()
            val originalPhoto = model.state.value.receiptPhoto
            assertNotNull(originalPhoto)
            val gate = CompletableDeferred<AppResult<ExpenseReceiptDomain>>()
            fixture.receipts.saveGate = gate
            model.onSave()
            runCurrent()
            assertEquals(1, fixture.saveCalls)
            assertTrue(model.state.value.receiptPending)
            assertTrue(model.state.value.busy)
            model.onSave()
            model.onTitleChanged("Accidental edit")
            model.onReceiptDiscarded()
            runCurrent()
            assertEquals("Groceries", model.state.value.title)
            assertEquals(originalPhoto, model.state.value.receiptPhoto)
            assertEquals(1, fixture.receipts.saveCalls)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertFalse(model.state.value.busy)
            assertNotNull(model.state.value.receiptError)
            assertNull(model.state.value.redirectTo)
            assertEquals(originalPhoto, model.state.value.receiptPhoto)
            fixture.receipts.saveGate = null
            model.onSave()
            runCurrent()
            assertEquals(1, fixture.saveCalls, "Photo retry must not repeat the financial write")
            assertEquals(2, fixture.receipts.saveCalls)
            assertEquals(
                ExpenseFormScreenViewModelContract.RedirectTo.Saved,
                model.state.value.redirectTo,
            )
            assertFalse(model.state.value.receiptPending)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aFailedOptionalPhotoCanBeLeftOutWithoutLosingTheSavedExpense() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        fixture.receipts.saveError = CommonError.NoConnection()
        val store = ViewModelStore()
        val model = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            model.onTitleChanged("Dinner")
            model.onAmountChanged("18.00")
            model.onReceiptPicked(byteArrayOf(1))
            runCurrent()
            model.onSave()
            runCurrent()
            assertTrue(model.state.value.receiptPending)
            model.onKeepExpense()
            runCurrent()
            assertEquals(1, fixture.saveCalls)
            assertEquals(
                ExpenseFormScreenViewModelContract.RedirectTo.Saved,
                model.state.value.redirectTo,
            )
            assertNull(model.state.value.receiptPhoto)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aBadPhotoLeavesTheExpenseDraftEditableAndOptional() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        fixture.accountResult.complete(AppResult.Success(fixture.account))
        fixture.membersResult.complete(AppResult.Success(fixture.members))
        fixture.receipts.prepareError = ReceiptError.InvalidPhoto()
        val store = ViewModelStore()
        val model = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            model.onTitleChanged("Train")
            model.onAmountChanged("12.00")
            model.onReceiptPicked(byteArrayOf(1))
            runCurrent()
            assertNotNull(model.state.value.receiptError)
            assertNull(model.state.value.receiptPhoto)
            assertTrue(model.state.value.canSave)
            model.onSave()
            runCurrent()
            assertEquals(1, fixture.saveCalls)
            assertEquals(0, fixture.receipts.saveCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
