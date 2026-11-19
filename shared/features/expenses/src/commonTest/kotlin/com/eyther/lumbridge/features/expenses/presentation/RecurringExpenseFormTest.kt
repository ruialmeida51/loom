package com.eyther.lumbridge.features.expenses.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.data.fixtures.RecurringExpenseFixtureDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.RecurringExpenseFormScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormScreenViewModelContract
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpenseFormTest {
    @Test
    fun failedPausedEditPreservesDraftVersionAndSplitAndBlocksDuplicateSaves() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val references =
            ExpenseFormFixture().apply {
                accountResult.complete(AppResult.Success(account))
                membersResult.complete(AppResult.Success(members))
            }
        val source = RecurringExpenseFixtureDataSource()
        val store = ViewModelStore()
        val model =
            RecurringExpenseFormScreenViewModel(
                    params =
                        RecurringExpenseFormScreenViewModelContract.Params(
                            accountId = "home",
                            ruleId = "rule",
                        ),
                    getRecurringExpenseFormUseCase =
                        GetRecurringExpenseFormUseCase(
                            recurringExpenseDataSource = source,
                            expenseAccountDataSource = references.accounts,
                            householdDataSource = references.household,
                            context = references.context,
                        ),
                    saveRecurringExpenseUseCase = SaveRecurringExpenseUseCase(source),
                    context = references.context,
                )
                .also { store.put("form", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertTrue(model.state.value.canSave)
            model.onAmountChanged("40.00")
            model.onTitleChanged("Monthly rent")
            runCurrent()
            val gate =
                CompletableDeferred<
                    AppResult<
                        com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain,
                    >,
                >()
            source.saveGate = gate
            model.onSave()
            runCurrent()
            model.onSave()
            model.onAmountChanged("99.00")
            assertEquals(1, source.saves.size)
            assertFalse(source.saves.single().active)
            assertEquals(mapOf("you" to 2000L, "alex" to 2000L), source.saves.single().shares)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertEquals("40.00", model.state.value.amount)
            assertEquals("Monthly rent", model.state.value.title)
            assertNotNull(model.state.value.error)
            assertFalse(model.state.value.saved)
            source.saveGate = null
            model.onSave()
            runCurrent()
            assertEquals(
                source.saves.first(),
                source.saves.last(),
                "Retry retains original version and draft",
            )
            assertTrue(model.state.value.saved)
            assertFalse(source.saves.last().active)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun anOwnerOutsideTheTripCanManageItsBillWithoutBeingAddedAsAParticipant() = runTest {
        val references =
            ExpenseFormFixture().apply {
                accountResult.complete(AppResult.Success(account.copy(memberIds = setOf("alex"))))
                membersResult.complete(AppResult.Success(members))
            }
        val source =
            RecurringExpenseFixtureDataSource().apply {
                rule =
                    rule.copy(
                        paidBy = "alex",
                        createdBy = "departed",
                        shares = mapOf("alex" to 3000L),
                    )
            }
        val result =
            GetRecurringExpenseFormUseCase(
                recurringExpenseDataSource = source,
                expenseAccountDataSource = references.accounts,
                householdDataSource = references.household,
                context = references.context,
            )(id = "rule", accountId = "home")
        val form = (result as AppResult.Success).value
        assertTrue(form.actorIsOwner)
        assertEquals(listOf("alex"), form.members.map { it.id })
    }

    @Test
    fun invalidAllocationFailsBeforeTheDataSourceAndCancelledHistoryCanStillBeDeletedByItsManager() =
        runTest {
            val source = RecurringExpenseFixtureDataSource()
            val result =
                SaveRecurringExpenseUseCase(source)(source.rule.copy(shares = mapOf("you" to 1)))
            assertTrue(
                result is AppResult.Failure && result.error is ExpenseError.IncorrectSplitTotal,
            )
            assertTrue(source.saves.isEmpty())
            val cancelled = source.rule.copy(cancelled = true)
            assertFalse(cancelled.canManage(userId = "you", isOwner = false))
            assertTrue(cancelled.canDelete(userId = "you", isOwner = false))
            assertFalse(cancelled.canDelete(userId = "outsider", isOwner = false))
        }
}
