package com.eyther.lumbridge.features.tasks.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.fixtures.TaskCreationFixtureDataSource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.AssignTaskUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class TaskTurnsTest {
    private val bins =
        TaskDomain(
            id = "bins",
            title = "Bins out",
            dueAt = null,
            recurrence = TaskRecurrenceDomain.WEEKLY,
            completed = false,
            assigneeId = "alex",
            rotation = listOf("you", "alex", "sam"),
        )

    @Test
    fun turnsGoRoundInOrderAndSkipPeopleWhoLeft() {
        assertEquals("sam", bins.nextAssignee(setOf("you", "alex", "sam")))
        assertEquals("you", bins.copy(assigneeId = "sam").nextAssignee(setOf("you", "alex", "sam")))
        assertEquals("you", bins.nextAssignee(setOf("you", "alex")))
        assertNull(bins.copy(rotation = emptyList()).nextAssignee(setOf("you", "alex")))
    }

    @Test
    fun everyoneTakesTurnsStartingWithTheChosenPerson() {
        assertEquals(
            listOf("you", "alex", "sam"),
            TaskDomain.rotationFor("sam", takesTurns = true, memberIds = listOf("you", "alex", "sam")),
        )
        assertEquals(
            emptyList(),
            TaskDomain.rotationFor(null, takesTurns = true, memberIds = listOf("you", "alex")),
        )
    }

    @Test
    fun onlyRepeatingTasksTakeTurns() = runTest {
        val source = TaskCreationFixtureDataSource()
        val context =
            HouseholdContextDomain(
                id = "home",
                userId = "you",
                name = "Home",
                currency = "EUR",
                timeZone = "UTC",
            )
        val add = AddTaskUseCase(taskDataSource = source, context = context)
        val once =
            add(
                title = "Fix shelf",
                date = LocalDate(2026, 11, 26),
                time = LocalTime(18, 0),
                recurrence = TaskRecurrenceDomain.NONE,
                assigneeId = "alex",
                takesTurns = true,
                memberIds = listOf("you", "alex"),
            )
        assertIs<TaskError.InvalidAssignee>(assertIs<AppResult.Failure>(once).error)
        val weekly =
            add(
                title = "Bins out",
                date = LocalDate(2026, 11, 26),
                time = LocalTime(18, 0),
                recurrence = TaskRecurrenceDomain.WEEKLY,
                assigneeId = "alex",
                takesTurns = true,
                memberIds = listOf("you", "alex"),
            )
        assertEquals(listOf("you", "alex"), assertIs<AppResult.Success<TaskDomain>>(weekly).value.rotation)

        val assigned = AssignTaskUseCase(source)(bins, assigneeId = null, takesTurns = true, listOf("you"))
        val task = assertIs<AppResult.Success<TaskDomain>>(assigned).value
        assertNull(task.assigneeId)
        assertEquals(emptyList(), task.rotation)
    }
}
