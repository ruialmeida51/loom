package com.eyther.lumbridge.features.tasks.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.fixtures.MemberFixtureDataSource
import com.eyther.lumbridge.features.tasks.data.fixtures.TaskCreationFixtureDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.AssignTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.CompleteTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.RemoveTaskUseCase
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.TasksScreenViewModel
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
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDraftTest {
    @Test
    fun removingTheDateClearsRepeatAndNotificationBeforeTitleOnlySave() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = TaskCreationFixtureDataSource()
        val context =
            HouseholdContextDomain(
                id = "home",
                userId = "you",
                name = "Home",
                currency = "EUR",
                timeZone = "Europe/Lisbon",
            )
        val store = ViewModelStore()
        val viewModel =
            TasksScreenViewModel(
                    context = context,
                    getTasksStreamUseCase = GetTasksStreamUseCase(taskDataSource = source),
                    addTaskUseCase = AddTaskUseCase(taskDataSource = source, context = context),
                    completeTaskUseCase = CompleteTaskUseCase(taskDataSource = source),
                    removeTaskUseCase = RemoveTaskUseCase(taskDataSource = source),
                    assignTaskUseCase = AssignTaskUseCase(taskDataSource = source),
                    getMembersStreamUseCase = MemberFixtureDataSource().useCase,
                )
                .also { store.put("tasks", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onAddSheetVisibilityChanged(true)
            viewModel.onTitleChanged("Fix the kitchen shelf")
            viewModel.onDateChanged(LocalDate(2026, 11, 20))
            viewModel.onNotificationEnabledChanged(true)
            viewModel.onRecurrenceChanged(TaskRecurrenceDomain.WEEKLY)
            runCurrent()
            assertTrue(viewModel.state.value.hasDueDate)
            viewModel.onDueDateEnabledChanged(false)
            runCurrent()
            assertFalse(viewModel.state.value.notificationEnabled)
            assertEquals(TaskRecurrenceDomain.NONE, viewModel.state.value.recurrence)
            viewModel.onRefresh()
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertEquals("Fix the kitchen shelf", viewModel.state.value.title)
            assertTrue(viewModel.state.value.showAdd)
            viewModel.onSaveTask()
            runCurrent()
            val saved = assertNotNull(source.created)
            assertNull(saved.dueAt)
            assertFalse(saved.notificationEnabled)
            assertFalse(viewModel.state.value.showAdd)
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
