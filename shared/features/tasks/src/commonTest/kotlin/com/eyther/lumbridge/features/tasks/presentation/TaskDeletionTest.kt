package com.eyther.lumbridge.features.tasks.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.fixtures.TaskCompletionFixtureDataSource
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
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
class TaskDeletionTest {
    @Test
    fun deletionNeedsConfirmationBlocksDoubleTapsAndCanRetryAReportedFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val context =
            HouseholdContextDomain(
                id = "home",
                userId = "you",
                name = "Home",
                currency = "EUR",
                timeZone = "UTC",
            )
        val source = TaskCompletionFixtureDataSource()
        val store = ViewModelStore()
        val model =
            TasksScreenViewModel(
                    context = context,
                    getTasksStreamUseCase = GetTasksStreamUseCase(source),
                    addTaskUseCase = AddTaskUseCase(taskDataSource = source, context = context),
                    completeTaskUseCase = CompleteTaskUseCase(source),
                    removeTaskUseCase = RemoveTaskUseCase(source),
                )
                .also { store.put("tasks", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            model.onRemoveTaskConfirmed()
            runCurrent()
            assertEquals(0, source.removalCalls)
            model.onRemoveTask(source.task.id)
            assertNotNull(model.state.value.deleteConfirmation)
            assertEquals(0, source.removalCalls)
            model.onRemoveTaskCancelled()
            assertNull(model.state.value.deleteConfirmation)
            assertEquals(source.task, model.state.value.items.single())
            val gate = CompletableDeferred<AppResult<Unit>>()
            source.removalGate = gate
            model.onRemoveTask(source.task.id)
            model.onRemoveTaskConfirmed()
            runCurrent()
            model.onRemoveTaskConfirmed()
            model.onRemoveTaskCancelled()
            runCurrent()
            assertEquals(1, source.removalCalls)
            assertTrue(model.state.value.deleting)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertFalse(model.state.value.deleting)
            assertNotNull(model.state.value.deleteError)
            assertNotNull(model.state.value.deleteConfirmation)
            assertEquals(source.task, model.state.value.items.single())
            source.removalGate = null
            model.onRemoveTaskConfirmed()
            runCurrent()
            assertEquals(2, source.removalCalls)
            assertNull(model.state.value.deleteConfirmation)
            assertTrue(model.state.value.items.isEmpty())
            assertNotNull(model.state.value.successMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
