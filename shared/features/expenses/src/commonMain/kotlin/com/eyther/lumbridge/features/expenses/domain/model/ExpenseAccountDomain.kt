package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseAccountDomain(
    val id: String,
    val name: String,
    val currency: String,
    val memberIds: Set<String>,
    val isDefault: Boolean = false,
    val archived: Boolean = false,
    val version: Long = 0,
    val createdBy: String = "",
    val guests: List<ExpenseGuestDomain> = emptyList(),
) {
    /** Everyone who can appear on this account's records: members and guests. */
    val participantIds: Set<String> = memberIds + guests.map { it.id }

    /** Only trip and event accounts take guests; Everyday is the household itself. */
    val acceptsGuests: Boolean = !isDefault

    /** Guests still on the account, in the order they were added. */
    val activeGuests: List<ExpenseGuestDomain> = guests.filterNot { it.removed }

    fun canAddExpense(userId: String): Boolean = !archived && userId in memberIds

    /** [members] followed by this account's guests, as one participant list. */
    fun withGuests(members: List<MemberDomain>): List<MemberDomain> =
        members + guests.map { it.asParticipant() }.filter { guest -> members.none { it.id == guest.id } }

    fun canManage(userId: String, isOwner: Boolean): Boolean =
        !isDefault && (createdBy == userId || isOwner)
}
