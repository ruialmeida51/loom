package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
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
class ExpenseCaptureTest {
    @Test
    fun independentReferenceReadsStartTogetherAndDoNotReplaceEarlyTyping() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        val store = ViewModelStore()
        val viewModel = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            assertEquals(1, fixture.referenceAccountRequests)
            assertEquals(1, fixture.referenceMemberRequests)
            viewModel.onAmountChanged("24.00")
            viewModel.onTitleChanged("Dinner")
            runCurrent()
            assertTrue(viewModel.state.value.loading)
            assertFalse(viewModel.state.value.canSave)
            fixture.accountResult.complete(AppResult.Success(fixture.account))
            fixture.membersResult.complete(AppResult.Success(fixture.members))
            runCurrent()
            assertEquals("Dinner", viewModel.state.value.title)
            assertEquals("24.00", viewModel.state.value.amount)
            assertEquals(setOf("you", "alex"), viewModel.state.value.split.participants)
            assertTrue(viewModel.state.value.canSave)
            fixture.saveResult = AppResult.Failure(CommonError.NoConnection())
            viewModel.onSave()
            runCurrent()
            assertNotNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.redirectTo)
            assertEquals("Dinner", viewModel.state.value.title)
            assertFalse(viewModel.state.value.busy)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aReferenceFailureCanBeRetriedWithoutErasingTheDraft() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = ExpenseFormFixture()
        val store = ViewModelStore()
        val viewModel = fixture.viewModel().also { store.put("expense", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            viewModel.onTitleChanged("Train tickets")
            viewModel.onAmountChanged("18.00")
            fixture.accountResult.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertNotNull(viewModel.state.value.error)
            fixture.accountResult = CompletableDeferred(AppResult.Success(fixture.account))
            fixture.membersResult.complete(AppResult.Success(fixture.members))
            viewModel.onRetry()
            runCurrent()
            assertNull(viewModel.state.value.error)
            assertEquals(1, fixture.referenceAccountRequests)
            assertEquals(1, fixture.accountRequests)
            assertEquals(1, fixture.memberRequests)
            assertEquals("Train tickets", viewModel.state.value.title)
            assertEquals("18.00", viewModel.state.value.amount)
            assertTrue(viewModel.state.value.canSave)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
