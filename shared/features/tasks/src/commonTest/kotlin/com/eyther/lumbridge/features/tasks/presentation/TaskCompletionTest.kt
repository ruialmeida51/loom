package com.eyther.lumbridge.features.tasks.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.fixtures.MemberFixtureDataSource
import com.eyther.lumbridge.features.tasks.data.fixtures.TaskCompletionFixtureDataSource
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

@OptIn(ExperimentalCoroutinesApi::class)
class TaskCompletionTest {
    @Test
    fun dailyCompletionHasProgressAndFeedbackWithoutCompletingTheNextOccurrence() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = TaskCompletionFixtureDataSource()
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
            viewModel.onCompleteTask(source.task)
            viewModel.onCompleteTask(source.task)
            runCurrent()
            assertTrue("daily" in viewModel.state.value.completingIds)
            assertEquals(1, source.calls)
            assertNull(viewModel.state.value.successMessage)
            source.items.value =
                AppResult.Success(
                    listOf(
                        source.task.copy(dueAt = kotlin.time.Instant.parse("2026-11-17T18:00:00Z")),
                    ),
                )
            source.completion.complete(AppResult.Success(Unit))
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertFalse("daily" in viewModel.state.value.completingIds)
            assertFalse(viewModel.state.value.items.single().completed)
            assertEquals(
                "2026-11-17T18:00:00Z",
                viewModel.state.value.items.single().dueAt.toString(),
            )
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
