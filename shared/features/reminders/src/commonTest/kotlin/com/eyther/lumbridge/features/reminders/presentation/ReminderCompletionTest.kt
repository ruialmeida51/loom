package com.eyther.lumbridge.features.reminders.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.data.fixtures.ReminderCompletionFixtureDataSource
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.RemindersScreenViewModel
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderCompletionTest {
    @Test
    fun dailyCompletionHasProgressAndFeedbackWithoutCompletingTheNextOccurrence() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ReminderCompletionFixtureDataSource()
        val context =
            HouseholdContextDomain(
                id = "home",
                userId = "you",
                name = "Home",
                currency = "EUR",
                timeZone = "UTC",
            )
        val store = ViewModelStore()
        val viewModel =
            RemindersScreenViewModel(
                    context = context,
                    getRemindersStreamUseCase =
                        GetRemindersStreamUseCase(reminderDataSource = source),
                    addReminderUseCase =
                        AddReminderUseCase(reminderDataSource = source, context = context),
                    completeReminderUseCase = CompleteReminderUseCase(reminderDataSource = source),
                    removeReminderUseCase = RemoveReminderUseCase(reminderDataSource = source),
                )
                .also { store.put("reminders", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onCompleteReminder(source.reminder)
            viewModel.onCompleteReminder(source.reminder)
            runCurrent()
            assertTrue("daily" in viewModel.state.value.completingIds)
            assertEquals(1, source.calls)
            assertNull(viewModel.state.value.successMessage)
            source.items.value =
                AppResult.Success(listOf(source.reminder.copy(dueAt = "2026-11-17T18:00:00Z")))
            source.completion.complete(AppResult.Success(Unit))
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertFalse("daily" in viewModel.state.value.completingIds)
            assertFalse(viewModel.state.value.items.single().completed)
            assertEquals("2026-11-17T18:00:00Z", viewModel.state.value.items.single().dueAt)
            assertNotNull(viewModel.state.value.successMessage)
            advanceTimeBy(4_000)
            runCurrent()
            assertNull(viewModel.state.value.successMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
