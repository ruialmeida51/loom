package com.eyther.lumbridge.features.household.presentation.mapper

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_invite
import com.eyther.lumbridge.features.household.generated.resources.invitation_replace
import com.eyther.lumbridge.features.household.generated.resources.invitation_replace_message
import com.eyther.lumbridge.features.household.generated.resources.invitation_replace_title
import com.eyther.lumbridge.features.household.generated.resources.invitation_reusable_description
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke_message
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke_title
import com.eyther.lumbridge.features.household.generated.resources.management_delete
import com.eyther.lumbridge.features.household.generated.resources.management_delete_message
import com.eyther.lumbridge.features.household.generated.resources.management_delete_title
import com.eyther.lumbridge.features.household.generated.resources.management_demote
import com.eyther.lumbridge.features.household.generated.resources.management_demote_message
import com.eyther.lumbridge.features.household.generated.resources.management_demote_title
import com.eyther.lumbridge.features.household.generated.resources.management_departure_message
import com.eyther.lumbridge.features.household.generated.resources.management_leave
import com.eyther.lumbridge.features.household.generated.resources.management_leave_message
import com.eyther.lumbridge.features.household.generated.resources.management_leave_title
import com.eyther.lumbridge.features.household.generated.resources.management_promote
import com.eyther.lumbridge.features.household.generated.resources.management_promote_message
import com.eyther.lumbridge.features.household.generated.resources.management_promote_title
import com.eyther.lumbridge.features.household.generated.resources.management_remove
import com.eyther.lumbridge.features.household.generated.resources.management_remove_title
import com.eyther.lumbridge.features.household.presentation.model.HouseholdConfirmationContentUi
import com.eyther.lumbridge.features.household.presentation.model.HouseholdManagementActionUi

internal fun HouseholdManagementActionUi.toConfirmation(): HouseholdConfirmationContentUi =
    when (this) {
        is HouseholdManagementActionUi.MemberChange ->
            when (action) {
                HouseholdMemberActionDomain.PROMOTE ->
                    HouseholdConfirmationContentUi(
                        title = TextResource.Resource(Res.string.management_promote_title),
                        message =
                            TextResource.Resource(
                                Res.string.management_promote_message,
                                args = listOf(member.name),
                            ),
                        action = TextResource.Resource(Res.string.management_promote),
                        destructive = false,
                    )
                HouseholdMemberActionDomain.DEMOTE ->
                    HouseholdConfirmationContentUi(
                        title = TextResource.Resource(Res.string.management_demote_title),
                        message =
                            TextResource.Resource(
                                Res.string.management_demote_message,
                                args = listOf(member.name),
                            ),
                        action = TextResource.Resource(Res.string.management_demote),
                        destructive = true,
                    )
                HouseholdMemberActionDomain.REMOVE ->
                    HouseholdConfirmationContentUi(
                        title = TextResource.Resource(Res.string.management_remove_title),
                        message =
                            TextResource.Resource(
                                Res.string.management_departure_message,
                                args = listOf(member.name),
                            ),
                        action = TextResource.Resource(Res.string.management_remove),
                        destructive = true,
                        balanceMemberId = member.id,
                    )
                HouseholdMemberActionDomain.LEAVE ->
                    HouseholdConfirmationContentUi(
                        title = TextResource.Resource(Res.string.management_leave_title),
                        message = TextResource.Resource(Res.string.management_leave_message),
                        action = TextResource.Resource(Res.string.management_leave),
                        destructive = true,
                        balanceMemberId = member.id,
                    )
            }
        is HouseholdManagementActionUi.InvitationChange ->
            HouseholdConfirmationContentUi(
                title =
                    TextResource.Resource(
                        if (revoke) Res.string.invitation_revoke_title
                        else if (invitation != null) Res.string.invitation_replace_title
                        else Res.string.details_invite,
                    ),
                message =
                    TextResource.Resource(
                        if (revoke) Res.string.invitation_revoke_message
                        else if (invitation != null) Res.string.invitation_replace_message
                        else Res.string.invitation_reusable_description,
                    ),
                action =
                    TextResource.Resource(
                        if (revoke) Res.string.invitation_revoke
                        else if (invitation != null) Res.string.invitation_replace
                        else Res.string.details_invite,
                    ),
                destructive = revoke,
            )
        is HouseholdManagementActionUi.Delete ->
            HouseholdConfirmationContentUi(
                title = TextResource.Resource(Res.string.management_delete_title),
                message = TextResource.Resource(Res.string.management_delete_message),
                action = TextResource.Resource(Res.string.management_delete),
                destructive = true,
                requiresName = true,
            )
    }
