package com.eyther.lumbridge.features.household.domain.model

/** Legacy hash-only invitations can be replaced, but their original code cannot be recovered. */
data class HouseholdInvitationDomain(
    val id: String,
    val version: Long,
    val code: String?,
    val expiresAt: String,
    val reusable: Boolean,
)
