package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.fixtures.SettlementFixtureDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveSettlementUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SettlementMutationTest {
    private val payment =
        SettlementDomain(
            id = "payment",
            from = "you",
            to = "alex",
            amountMinor = 1250,
            occurredOn = "2026-11-18",
            accountId = "trip",
            createdBy = "you",
            version = 3,
        )

    @Test
    fun malformedDatesAndMissingVersionsAreTypedAndNeverReachTheWritePort() = runTest {
        val source = SettlementFixtureDataSource()
        val save = SaveSettlementUseCase(source)
        assertIs<ExpenseError.InvalidPaymentDate>(
            assertIs<AppResult.Failure>(save(payment.copy(occurredOn = "not-a-date"))).error,
        )
        assertIs<SettlementError.Changed>(
            assertIs<AppResult.Failure>(save(payment.copy(version = 0))).error,
        )
        assertIs<SettlementError.Changed>(
            assertIs<AppResult.Failure>(RemoveSettlementUseCase(source)(payment.copy(version = 0)))
                .error,
        )
        assertIs<ExpenseError.InvalidSettlement>(
            assertIs<AppResult.Failure>(save(payment.copy(to = "you"))).error,
        )
        assertEquals(0, source.saves)
        assertEquals(0, source.removals)
    }

    @Test
    fun managementMatchesTheOriginalSenderRecipientCreatorOrHouseholdOwner() {
        assertTrue(payment.canManage("you", false))
        assertTrue(payment.canManage("alex", false))
        assertTrue(payment.canManage("household-owner", true))
        assertFalse(payment.canManage("other-member", false))
    }
}
