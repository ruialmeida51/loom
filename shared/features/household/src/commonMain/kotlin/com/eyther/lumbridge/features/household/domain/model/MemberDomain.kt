package com.eyther.lumbridge.features.household.domain.model

/** Inactive identities remain available to financial history, without household access. */
data class MemberDomain(
    val id: String,
    val name: String,
    val role: String,
    val active: Boolean = true,
    val version: Long = 1,
) {
    val isOwner: Boolean
        get() = active && role == "owner"
}
