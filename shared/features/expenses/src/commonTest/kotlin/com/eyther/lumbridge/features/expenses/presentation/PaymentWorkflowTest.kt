package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.features.expenses.data.fixtures.PaymentWorkflowFixture
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
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

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentWorkflowTest {
    @Test
    fun archivedAccountsKeepBalancesReadableWithoutOfferingARecordPaymentAction() {
        val fixture = PaymentWorkflowFixture()
        val active =
            ExpensesState(
                userId = fixture.references.context.userId,
                account = fixture.references.account,
                members = fixture.references.members,
                balances = mapOf("you" to -1250L, "alex" to 1250L),
                loading = false,
            )
        assertTrue(active.suggestedRepayments.single().canRecord)
        val archived = active.copy(account = fixture.references.account.copy(archived = true))
        assertFalse(archived.suggestedRepayments.single().canRecord)
        assertEquals(
            active.suggestedRepayments.single().amount,
            archived.suggestedRepayments.single().amount,
        )
    }

    @Test
    fun editRetryRetainsInputsIdentityAndTheOriginalVersion() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = PaymentWorkflowFixture()
        val viewModel = fixture.form()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertEquals("12.50", viewModel.state.value.amount)
            viewModel.onAmountChanged("20.00")
            viewModel.onDateChanged("2026-11-17")
            runCurrent()
            fixture.source.mutationError = CommonError.NoConnection()
            viewModel.onSave()
            runCurrent()
            assertNotNull(viewModel.state.value.error)
            assertEquals("20.00", viewModel.state.value.amount)
            assertEquals("2026-11-17", viewModel.state.value.occurredOn)
            fixture.source.mutationError = null
            viewModel.onSave()
            runCurrent()
            assertTrue(viewModel.state.value.saved)
            assertEquals(listOf(3L, 3L), fixture.source.savedPayments.map { it.version })
            assertEquals(listOf("payment", "payment"), fixture.source.savedPayments.map { it.id })
            assertEquals(2000, fixture.source.payment?.amountMinor)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun suggestionSeedsTheDraftAndDuplicateSavesCannotRecordTwoPayments() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = PaymentWorkflowFixture()
        val viewModel =
            fixture.form(
                editing = false,
                suggestion = SuggestedSettlementDomain("alex", "you", 3499),
            )
        val gate = CompletableDeferred<Unit>()
        fixture.source.mutationGate = gate
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertEquals("34.99", viewModel.state.value.amount)
            assertEquals("alex", viewModel.state.value.from)
            assertEquals("you", viewModel.state.value.to)
            viewModel.onSave()
            viewModel.onSave()
            runCurrent()
            assertEquals(1, fixture.source.records)
            assertTrue(viewModel.state.value.busy)
            viewModel.onAmountChanged("99.00")
            gate.complete(Unit)
            runCurrent()
            assertEquals(3499, fixture.source.payment?.amountMinor)
            assertTrue(viewModel.state.value.saved)
            viewModel.onSave()
            runCurrent()
            assertEquals(1, fixture.source.records)
        } finally {
            gate.complete(Unit)
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun returningAfterAnEditUpdatesDetailsWithoutARefreshTransition() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = PaymentWorkflowFixture()
        val viewModel = fixture.details()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            assertEquals(1250, viewModel.state.value.payment?.amountMinor)
            fixture.source.payment = fixture.payment.copy(amountMinor = 4200, version = 4)
            viewModel.onContentChanged()
            runCurrent()
            assertFalse(viewModel.state.value.refreshing)
            assertFalse(viewModel.state.value.loading)
            assertEquals(4200, viewModel.state.value.payment?.amountMinor)
            viewModel.onRefresh()
            runCurrent()
            assertTrue(viewModel.state.value.refreshing)
            advanceTimeBy(350)
            runCurrent()
            assertFalse(viewModel.state.value.refreshing)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedDeletionKeepsTheFrozenTargetAndConfirmationUntilRetryOrCancel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = PaymentWorkflowFixture()
        val viewModel = fixture.details()
        val gate = CompletableDeferred<Unit>()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            advanceTimeBy(350)
            runCurrent()
            viewModel.onDeleteRequested()
            fixture.source.payment = fixture.payment.copy(amountMinor = 9999, version = 4)
            fixture.source.mutationError = SettlementError.Changed()
            fixture.source.mutationGate = gate
            viewModel.onDeleteConfirmed()
            viewModel.onDeleteConfirmed()
            viewModel.onDeleteCancelled()
            viewModel.onRefresh()
            runCurrent()
            assertEquals(1, fixture.source.removals)
            assertNotNull(viewModel.state.value.deleteConfirmation)
            gate.complete(Unit)
            runCurrent()
            assertNotNull(viewModel.state.value.deleteError)
            assertEquals(3, viewModel.state.value.deletingPayment?.version)
            fixture.source.mutationError = null
            viewModel.onDeleteConfirmed()
            runCurrent()
            assertTrue(viewModel.state.value.deleted)
            assertEquals(listOf(3L, 3L), fixture.source.removedPayments.map { it.version })
        } finally {
            gate.complete(Unit)
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
