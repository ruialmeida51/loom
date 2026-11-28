package com.eyther.lumbridge.features.home.presentation

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.home.domain.model.MemberProfileDomain
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.MemberProfileState
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberProfileStateTest {
    private val you = MemberDomain(id = "you", name = "You", role = "owner")
    private val alex = MemberDomain(id = "alex", name = "Alex", role = "member")
    private val sam = MemberDomain(id = "sam", name = "Sam", role = "member")
    private val handles = PaymentHandlesDomain(userId = "alex", paypalUsername = "AlexS")

    private fun state(
        member: MemberDomain,
        balances: Map<String, Long>,
        tasks: List<TaskDomain> = emptyList(),
    ) =
        MemberProfileState(
            userId = "you",
            currency = "EUR",
            today = "2026-11-28",
            timeZone = "UTC",
            loading = false,
            profile =
                MemberProfileDomain(
                    member = member,
                    members = listOf(you, alex, sam),
                    handles = handles.takeIf { member == alex },
                    balances = balances,
                    tasks = tasks,
                ),
        )

    private fun task(id: String, assignee: String?, completed: Boolean = false) =
        TaskDomain(
            id = id,
            title = id,
            dueAt = null,
            recurrence = TaskRecurrenceDomain.NONE,
            completed = completed,
            assigneeId = assignee,
        )

    @Test
    fun repaymentsInvolvingTheMemberAreListedFromYourSide() {
        // You owe Alex €12; Sam owes Alex €3.
        val settlements =
            state(alex, mapOf("you" to -1200L, "alex" to 1500L, "sam" to -300L)).settlements
        assertEquals(2, settlements.size)
        val yours = settlements.first { it.fromIsYou }
        assertEquals("Alex", yours.toName)
        assertEquals("€12.00", yours.amount)
        val sams = settlements.first { !it.fromIsYou && !it.toIsYou }
        assertEquals("Sam", sams.fromName)
        assertEquals("sam", sams.otherId)
    }

    @Test
    fun whenYouOweThemPayPalOpensWithThatAmount() {
        val options = state(alex, mapOf("you" to -1200L, "alex" to 1200L)).payOptions
        assertEquals("12.00", options?.amount)
        assertEquals("https://paypal.me/AlexS/12.00EUR", options?.paypalLink)
    }

    @Test
    fun otherwiseTheirPayPalPageIsOffered() {
        val options = state(alex, mapOf("you" to 500L, "alex" to -500L)).payOptions
        assertNull(options?.amount)
        assertEquals("https://paypal.me/AlexS", options?.paypalLink)
    }

    @Test
    fun yourOwnProfileOffersNothingToPay() {
        val own = state(you, mapOf("you" to 500L, "alex" to -500L))
        assertTrue(own.isCurrentUser)
        assertNull(own.payOptions)
    }

    @Test
    fun onlyTheirOpenTasksAreShown() {
        val tasks =
            state(
                    alex,
                    emptyMap(),
                    tasks =
                        listOf(
                            task("bins", "alex"),
                            task("done", "alex", completed = true),
                            task("sams", "sam"),
                            task("anyone", null),
                        ),
                )
                .tasks
        assertEquals(listOf("bins"), tasks.map { it.id })
    }
}
