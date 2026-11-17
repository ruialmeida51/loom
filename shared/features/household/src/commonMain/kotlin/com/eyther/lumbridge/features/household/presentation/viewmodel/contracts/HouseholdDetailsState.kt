package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke_message
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke_title
import com.eyther.lumbridge.features.household.generated.resources.ui_cancel
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HouseholdDetailsState(
    val userId: String,
    val members: List<MemberDomain> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val invitation: String? = null,
    val confirmingRevoke: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {
    val revokeConfirmation: DestructiveConfirmationUi? =
        if (confirmingRevoke) {
            DestructiveConfirmationUi(
                title = TextResource.Resource(Res.string.invitation_revoke_title),
                message = TextResource.Resource(Res.string.invitation_revoke_message),
                confirmText = TextResource.Resource(Res.string.invitation_revoke),
                cancelText = TextResource.Resource(Res.string.ui_cancel),
            )
        } else null
    val displayError: TextResource? = error?.takeIf { invitation == null } ?: loadError
    val invitationError: TextResource? = error?.takeUnless { confirmingRevoke }
    val revokeError: TextResource? = error?.takeIf { confirmingRevoke }
    val isOwner: Boolean = members.firstOrNull { it.id == userId }?.role == "owner"
    val canInvite: Boolean = isOwner && !busy
    val memberRows: ImmutableList<HouseholdMemberUi> =
        members
            .map { member ->
                HouseholdMemberUi(
                    id = member.id,
                    name = member.name,
                    initials =
                        member.name
                            .firstOrNull { it.isLetterOrDigit() }
                            ?.uppercaseChar()
                            ?.toString()
                            .orEmpty(),
                    isOwner = member.role == "owner",
                    isCurrentUser = member.id == userId,
                )
            }
            .sortedWith(
                compareByDescending<HouseholdMemberUi> { it.isOwner }
                    .thenBy { it.name.lowercase() }
                    .thenBy { it.id },
            )
            .toImmutableList()
}
