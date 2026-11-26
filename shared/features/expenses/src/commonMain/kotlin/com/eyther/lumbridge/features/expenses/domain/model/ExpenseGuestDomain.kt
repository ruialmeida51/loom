package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

/** The role a guest takes when listed with household members as an account participant. */
const val GUEST_ROLE = "guest"

/**
 * Someone outside the household who takes part in one trip or event account. Household members
 * record their expenses and payments; [removed] guests stay on the records they were part of.
 */
data class ExpenseGuestDomain(
    val id: String,
    val name: String,
    val removed: Boolean = false,
    val version: Long = 0,
) {
    /** The guest as a participant, so splits, balances and names treat them like a member. */
    fun asParticipant(): MemberDomain =
        MemberDomain(id = id, name = name, role = GUEST_ROLE, active = !removed, version = version)

    /** A name of 1 to [MAXIMUM_NAME_LENGTH] characters once trimmed. */
    fun hasValidName(): Boolean = name.trim().length in 1..MAXIMUM_NAME_LENGTH

    companion object {
        const val MAXIMUM_NAME_LENGTH = 40
        const val MAXIMUM_PER_ACCOUNT = 20
    }
}

/** Whether this participant is a guest rather than a household member. */
val MemberDomain.isGuest: Boolean
    get() = role == GUEST_ROLE
