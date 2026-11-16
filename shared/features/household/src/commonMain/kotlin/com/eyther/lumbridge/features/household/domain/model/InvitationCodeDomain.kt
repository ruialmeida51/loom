package com.eyther.lumbridge.features.household.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError

/** New invitations are six unambiguous characters; valid legacy tokens can still be pasted. */
object InvitationCodeDomain {
    const val LENGTH = 6
    const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private const val LEGACY_LENGTH = 64

    fun isComplete(value: String): Boolean =
        (value.length == LENGTH && value.uppercase().all { it in ALPHABET }) ||
            (value.length == LEGACY_LENGTH && value.all { it in "0123456789abcdefABCDEF" })

    fun normalized(value: String): String {
        val trimmed = value.trim()
        requireDomain(isComplete(trimmed)) { HouseholdError.InvalidInvitation() }
        return if (trimmed.length == LENGTH) trimmed.uppercase() else trimmed.lowercase()
    }
}
