package com.eyther.lumbridge.features.tasks.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.fixtures.TaskCreationFixtureDataSource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class TaskCreationTest {
    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "Europe/Lisbon",
        )

    @Test
    fun aTitleAloneCreatesANonNotifyingTask() = runTest {
        val source = TaskCreationFixtureDataSource()
        val result =
            AddTaskUseCase(taskDataSource = source, context = context)(
                title = "  Fix the kitchen shelf  ",
                date = null,
                time = LocalTime(18, 0),
                recurrence = TaskRecurrenceDomain.NONE,
            )
        assertIs<AppResult.Success<*>>(result)
        val task = assertNotNull(source.created)
        assertEquals("Fix the kitchen shelf", task.title)
        assertNull(task.dueAt)
        assertFalse(task.notificationEnabled)
    }

    @Test
    fun aDueDateDoesNotImplyANotification() = runTest {
        val source = TaskCreationFixtureDataSource()
        AddTaskUseCase(taskDataSource = source, context = context)(
            title = "Parcel",
            date = LocalDate(2026, 3, 29),
            time = LocalTime(9, 0),
            recurrence = TaskRecurrenceDomain.NONE,
        )
        val task = assertNotNull(source.created)
        assertEquals(Instant.parse("2026-03-29T08:00:00Z"), task.dueAt)
        assertFalse(task.notificationEnabled)
    }

    @Test
    fun repeatingOrNotifyingRequiresADateBeforeAnyWrite() = runTest {
        val source = TaskCreationFixtureDataSource()
        val add = AddTaskUseCase(taskDataSource = source, context = context)
        val notification =
            add(
                title = "Shelf",
                date = null,
                time = LocalTime(18, 0),
                recurrence = TaskRecurrenceDomain.NONE,
                notificationEnabled = true,
            )
        val repeating =
            add(
                title = "Shelf",
                date = null,
                time = LocalTime(18, 0),
                recurrence = TaskRecurrenceDomain.DAILY,
            )
        assertIs<TaskError.DateRequired>(assertIs<AppResult.Failure>(notification).error)
        assertIs<TaskError.DateRequired>(assertIs<AppResult.Failure>(repeating).error)
        assertNull(source.created)
    }
}
