package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.domain.model.isGuest
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveAccountGuestUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

class AccountGuestsTest {
    private val members =
        listOf(MemberDomain("you", "You", "owner"), MemberDomain("alex", "Alex", "member"))
    private val marta = ExpenseGuestDomain(id = "marta", name = "Marta", version = 1)
    private val trip =
        ExpenseAccountDomain(
            id = "trip",
            name = "Porto",
            currency = "EUR",
            memberIds = setOf("you", "alex"),
            version = 1,
            guests = listOf(marta, ExpenseGuestDomain("old", "Rui", removed = true, version = 2)),
        )

    @Test
    fun guestsFollowMembersAsTaggedParticipantsAndOnlyTripsTakeThem() {
        val participants = trip.withGuests(members)
        assertEquals(listOf("you", "alex", "marta", "old"), participants.map { it.id })
        assertEquals(listOf(false, false, true, true), participants.map { it.isGuest })
        assertFalse(participants.last().active)
        assertEquals(setOf("you", "alex", "marta", "old"), trip.participantIds)
        assertEquals(listOf(marta), trip.activeGuests)
        assertTrue(trip.acceptsGuests)
        assertFalse(trip.copy(isDefault = true).acceptsGuests)
    }

    @Test
    fun anyAccountMemberRecordsAGuestsPaymentButNotOtherMembersPayments() {
        assertTrue(trip.canRecordPayment(userId = "you", from = "marta", to = "alex"))
        assertTrue(trip.canRecordPayment(userId = "you", from = "you", to = "alex"))
        assertFalse(trip.canRecordPayment(userId = "you", from = "alex", to = "sam"))
        assertFalse(trip.canRecordPayment(userId = "outsider", from = "marta", to = "alex"))
    }

    @Test
    fun guestNamesAreTrimmedAndBoundedBeforeReachingTheServer() = runTest {
        val source = GuestSource(trip)
        val save = SaveAccountGuestUseCase(source)

        assertIs<AppResult.Success<ExpenseAccountDomain>>(
            save(trip, ExpenseGuestDomain(id = "joana", name = "  Joana ")),
        )
        assertEquals("Joana", source.saved.single().name)

        assertIs<ExpenseAccountError.InvalidGuestName>(
            assertIs<AppResult.Failure>(save(trip, ExpenseGuestDomain("x", "   "))).error,
        )
        assertIs<ExpenseAccountError.InvalidGuestName>(
            assertIs<AppResult.Failure>(save(trip, ExpenseGuestDomain("x", "a".repeat(41)))).error,
        )
        assertIs<ExpenseAccountError.GuestsNotOnEveryday>(
            assertIs<AppResult.Failure>(
                    save(trip.copy(isDefault = true), ExpenseGuestDomain("x", "Ana")),
                )
                .error,
        )
        val full =
            trip.copy(
                guests = (1..ExpenseGuestDomain.MAXIMUM_PER_ACCOUNT).map {
                    ExpenseGuestDomain("g$it", "Guest $it", version = 1)
                },
            )
        assertIs<ExpenseAccountError.GuestLimit>(
            assertIs<AppResult.Failure>(save(full, ExpenseGuestDomain("x", "Ana"))).error,
        )
        // Renaming at the limit is still allowed.
        assertIs<AppResult.Success<ExpenseAccountDomain>>(
            save(full, full.guests.first().copy(name = "Renamed")),
        )
        assertEquals(2, source.saved.size)
    }

    @Test
    fun guestBalancesShowBesideMembersWithTheirNames() {
        val state =
            ExpensesState(
                userId = "you",
                account = trip,
                members = members,
                expenses =
                    listOf(
                        ExpenseDomain(
                            id = "guesthouse",
                            title = "Guesthouse",
                            amountMinor = 9000,
                            paidBy = "you",
                            category = "Other",
                            occurredOn = "2026-11-20",
                            shares = mapOf("you" to 3000L, "alex" to 3000L, "marta" to 3000L),
                            accountId = "trip",
                        ),
                    ),
                balances = mapOf("you" to 6000L, "alex" to -3000L, "marta" to -3000L),
            )
        val marta = state.memberBalances.single { it.member.id == "marta" }
        assertEquals("Marta", marta.member.name)
        assertTrue(marta.member.isGuest)
        assertFalse(marta.balance.isOwed)
        assertTrue(state.accountMembers.any { it.id == "marta" })
        assertFalse(state.memberBalances.any { it.member.id == "old" })
    }

    private class GuestSource(private val account: ExpenseAccountDomain) :
        ExpenseAccountDataSource {
        val saved = mutableListOf<ExpenseGuestDomain>()

        override fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>> = error("Not used")

        override fun streamSelection(): StateFlow<String> = MutableStateFlow(account.id)

        override suspend fun getAccount(id: String) = AppResult.Success(account)

        override suspend fun selectAccount(id: String): AppResult<Unit> = error("Not used")

        override suspend fun saveAccount(account: ExpenseAccountDomain) = AppResult.Success(account)

        override suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit> =
            error("Not used")

        override suspend fun saveGuest(accountId: String, guest: ExpenseGuestDomain) =
            AppResult.Success(account).also { saved += guest }

        override suspend fun removeGuest(accountId: String, guest: ExpenseGuestDomain) =
            AppResult.Success(account)
    }
}
