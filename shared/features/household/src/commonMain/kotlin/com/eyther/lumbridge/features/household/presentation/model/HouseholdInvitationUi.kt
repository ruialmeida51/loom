package com.eyther.lumbridge.features.household.presentation.model

import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HouseholdInvitationUi(val invitation: HouseholdInvitationDomain) {
    val expiry: LocalDateTime =
        Instant.parse(invitation.expiresAt).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour: String = expiry.hour.toString().padStart(2, '0')
    val minute: String = expiry.minute.toString().padStart(2, '0')
}
