package com.eyther.lumbridge.features.household.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

/** Frozen targets and request identities survive failed writes and explicit retries. */
@Stable
sealed interface HouseholdManagementActionUi {
    val requestId: String

    @Stable
    data class MemberChange(
        val member: MemberDomain,
        val action: HouseholdMemberActionDomain,
        override val requestId: String,
    ) : HouseholdManagementActionUi

    @Stable
    data class InvitationChange(
        val invitation: HouseholdInvitationDomain?,
        val revoke: Boolean,
        override val requestId: String,
    ) : HouseholdManagementActionUi

    @Stable data class Delete(override val requestId: String) : HouseholdManagementActionUi
}
