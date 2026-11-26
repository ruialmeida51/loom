package com.eyther.lumbridge.features.tasks.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class TaskDomain(
    val id: String,
    val title: String,
    val dueAt: Instant?,
    val recurrence: TaskRecurrenceDomain,
    val completed: Boolean,
    val notificationEnabled: Boolean = false,
    /** Whose job this is; null means anyone in the household. */
    val assigneeId: String? = null,
    /** Who takes turns, in order. Completing an occurrence passes it to the next one. */
    val rotation: List<String> = emptyList(),
) {
    val takesTurns: Boolean = rotation.isNotEmpty()

    /** Whose turn comes after the current one, among [activeIds]; null without turns. */
    fun nextAssignee(activeIds: Set<String>): String? {
        if (!takesTurns) return null
        val start = rotation.indexOf(assigneeId)
        return (1..rotation.size)
            .map { offset -> rotation[(start + offset).mod(rotation.size)] }
            .firstOrNull { it in activeIds && it != assigneeId }
    }

    /** Turns need a repeating task and must include the person whose turn it is now. */
    fun hasValidPeople(): Boolean =
        !takesTurns ||
            (recurrence != TaskRecurrenceDomain.NONE &&
                assigneeId in rotation &&
                rotation.distinct().size == rotation.size &&
                rotation.size <= MAXIMUM_ROTATION)

    /** A repeat stays at the household's wall-clock time across daylight-saving changes. */
    fun nextDue(zone: String): Instant? {
        val due = dueAt ?: return null
        val period = recurrence.period ?: return null
        val timeZone = TimeZone.of(zone)
        val local = due.toLocalDateTime(timeZone)
        return LocalDateTime(local.date.plus(period), local.time).toInstant(timeZone)
    }

    companion object {
        const val MAXIMUM_ROTATION = 20

        /**
         * Everyone in [memberIds] takes turns, in that order, when [takesTurns] and someone is
         * assigned; otherwise no rotation.
         */
        fun rotationFor(assigneeId: String?, takesTurns: Boolean, memberIds: List<String>) =
            if (takesTurns && assigneeId != null) (memberIds + assigneeId).distinct()
            else emptyList()
    }
}
