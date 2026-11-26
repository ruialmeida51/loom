package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
class ExpenseCurrencyFormTest {
    @Test
    fun anExpensePaidInPoundsSavesInEurosAndKeepsWhatWasPaid() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        val store = ViewModelStore()
        val viewModel = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            fixture.accountResult.complete(AppResult.Success(fixture.account))
            fixture.membersResult.complete(AppResult.Success(fixture.members))
            runCurrent()
            viewModel.onTitleChanged("Fish market")
            viewModel.onAmountChanged("48.00")
            viewModel.onEntryCurrencyChanged("GBP")
            runCurrent()
            assertEquals("GBP", viewModel.state.value.amountCurrency)
            assertEquals("1.1546", viewModel.state.value.rate)
            assertEquals("€55.42", viewModel.state.value.convertedAmount)
            assertEquals("£24.00", viewModel.state.value.equalShare)
            viewModel.onSave()
            runCurrent()
            val saved = assertNotNull(fixture.saved)
            assertEquals(5542L, saved.amountMinor)
            assertEquals(5542L, saved.shares.values.sum())
            assertEquals(OriginalAmountDomain("GBP", 4800, "1.1546"), saved.original)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun withoutTodaysRateThePersonTypesOneAndRecurringStaysInEuros() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture().apply { rateResult = AppResult.Failure(ExpenseError.RateUnavailable()) }
        val store = ViewModelStore()
        val viewModel = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            fixture.accountResult.complete(AppResult.Success(fixture.account))
            fixture.membersResult.complete(AppResult.Success(fixture.members))
            runCurrent()
            viewModel.onTitleChanged("Taxi")
            viewModel.onAmountChanged("20")
            viewModel.onEntryCurrencyChanged("USD")
            runCurrent()
            assertNotNull(viewModel.state.value.rateError)
            assertFalse(viewModel.state.value.canSave)
            viewModel.onRateChanged("0,95")
            runCurrent()
            assertEquals("0.95", viewModel.state.value.rate)
            assertTrue(viewModel.state.value.rateIsCustom)
            assertEquals("€19.00", viewModel.state.value.convertedAmount)
            assertTrue(viewModel.state.value.canSave)
            viewModel.onRecurringChanged(true)
            runCurrent()
            assertNull(viewModel.state.value.entryCurrency)
            assertEquals("EUR", viewModel.state.value.amountCurrency)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
