package com.eyther.lumbridge.features.household.domain

import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.InvitationCodeDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class InvitationCodeTest {
    @Test
    fun shortCodesAreCaseInsensitiveAndLegacyTokensRemainPasteable() {
        assertEquals("ABC234", InvitationCodeDomain.normalized("  abc234  "))
        assertEquals("a".repeat(64), InvitationCodeDomain.normalized("A".repeat(64)))
    }

    @Test
    fun ambiguousOrIncompleteCodesCannotBeSubmitted() {
        assertFalse(InvitationCodeDomain.isComplete("AB0123"))
        assertFalse(InvitationCodeDomain.isComplete("ABC23"))
        assertFailsWith<HouseholdError.InvalidInvitation> {
            InvitationCodeDomain.normalized("A".repeat(20))
        }
    }
}
