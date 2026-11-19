package com.eyther.lumbridge.features.household.domain.model

data class HouseholdManagementDomain(
    val members: List<MemberDomain>,
    val invitation: HouseholdInvitationDomain?,
) {
    fun isOwner(userId: String): Boolean = members.any { it.id == userId && it.isOwner }

    fun canManageMember(actorId: String, memberId: String): Boolean =
        isOwner(userId = actorId) &&
            actorId != memberId &&
            members.any { it.id == memberId && it.active }

    fun canLeave(userId: String): Boolean =
        members.any { it.id == userId && it.active } &&
            (!isOwner(userId = userId) || members.count { it.isOwner } > 1)
}
