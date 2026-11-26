package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.io.bytestring.ByteString

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseReceiptPreviewTest {
    @Test
    fun anAttachedPhotoLoadsAsAPreviewAndCannotBeSavedAsAnUploadDraft() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = readyFixture()
        val store = ViewModelStore()
        val model = fixture.detailsViewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch { model.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertNotNull(model.state.value.receiptPreviewUi)
            assertNull(model.state.value.receiptPhoto)
            assertFalse(model.state.value.receiptPreviewLoading)
            assertEquals("old.jpg", model.state.value.receiptPreview?.id)
            model.onReceiptSave()
            runCurrent()
            assertEquals(0, fixture.receipts.saveCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aFailedPreviewCanRetryWithoutReplacingTheExpense() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = readyFixture().also { it.receipts.photo = null }
        val store = ViewModelStore()
        val model = fixture.detailsViewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch { model.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertNotNull(model.state.value.receiptError)
            assertFalse(model.state.value.receiptPreviewLoading)
            assertEquals("Dinner", model.state.value.expense?.title)
            fixture.receipts.photo = ReceiptPhotoDomain(data = ByteString(byteArrayOf(1, 2, 3)))
            model.onReceiptRetry()
            runCurrent()
            assertNotNull(model.state.value.receiptPreviewUi)
            assertNull(model.state.value.receiptError)
            assertEquals("Dinner", model.state.value.expense?.title)
            assertEquals(0, fixture.saveCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aFailedReplacementKeepsItsDraftAndDiscardRevealsTheStoredPreview() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = readyFixture()
        val store = ViewModelStore()
        val model = fixture.detailsViewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch { model.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            val original = model.state.value.receiptPreview
            assertNotNull(original)
            model.onReceiptPicked(byteArrayOf(4, 5, 6))
            runCurrent()
            val draft = model.state.value.receiptPhoto
            assertNotNull(draft)
            fixture.receipts.saveError = CommonError.NoConnection()
            model.onReceiptSave()
            runCurrent()
            assertEquals(draft, model.state.value.receiptPhoto)
            assertEquals(original, model.state.value.receiptPreview)
            assertNotNull(model.state.value.receiptError)
            assertFalse(model.state.value.receiptPreviewLoading)
            model.onReceiptDiscarded()
            assertNull(model.state.value.receiptPhoto)
            assertEquals(original, model.state.value.receiptPreview)
            assertEquals(0, fixture.saveCalls)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun readyFixture(): ExpenseFormFixture =
        ExpenseFormFixture().also { fixture ->
            fixture.accountResult.complete(AppResult.Success(fixture.account))
            fixture.membersResult.complete(AppResult.Success(fixture.members))
            fixture.loadedExpense =
                ExpenseDomain(
                    id = "expense",
                    title = "Dinner",
                    amountMinor = 2000,
                    paidBy = "you",
                    category = "Other",
                    occurredOn = LocalDate.parse("2026-11-19"),
                    shares = mapOf("you" to 1000, "alex" to 1000),
                    accountId = fixture.account.id,
                    version = 3,
                    createdBy = "you",
                )
            fixture.receipts.receipt =
                ExpenseReceiptDomain(
                    expenseId = "expense",
                    accountId = fixture.account.id,
                    objectPath = "old.jpg",
                    version = 1,
                )
            fixture.receipts.photo = ReceiptPhotoDomain(data = ByteString(byteArrayOf(1, 2, 3)))
        }
}
