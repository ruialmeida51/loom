package com.eyther.lumbridge.features.household.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.error_already_joined
import com.eyther.lumbridge.features.household.generated.resources.error_expired_invitation
import com.eyther.lumbridge.features.household.generated.resources.error_household_full
import com.eyther.lumbridge.features.household.generated.resources.error_invalid_invitation
import com.eyther.lumbridge.features.household.generated.resources.error_invalid_name
import com.eyther.lumbridge.features.household.generated.resources.error_invitation_rate_limited
import com.eyther.lumbridge.features.household.generated.resources.error_owner_required
import com.eyther.lumbridge.features.household.generated.resources.error_unsupported_currency
import com.eyther.lumbridge.features.household.generated.resources.management_error_access
import com.eyther.lumbridge.features.household.generated.resources.management_error_changed
import com.eyther.lumbridge.features.household.generated.resources.management_error_invitation_changed
import com.eyther.lumbridge.features.household.generated.resources.management_error_invitation_failed
import com.eyther.lumbridge.features.household.generated.resources.management_error_last_owner
import com.eyther.lumbridge.features.household.generated.resources.management_error_name
import com.eyther.lumbridge.features.household.generated.resources.management_error_share

fun AppError.toHouseholdTextResource(): TextResource =
    when (this) {
        is HouseholdError.AccessDenied -> TextResource.Resource(Res.string.management_error_access)
        is HouseholdError.Changed -> TextResource.Resource(Res.string.management_error_changed)
        is HouseholdError.LastOwner -> TextResource.Resource(Res.string.management_error_last_owner)
        is HouseholdError.NameConfirmation ->
            TextResource.Resource(Res.string.management_error_name)
        is HouseholdError.InvitationChanged ->
            TextResource.Resource(Res.string.management_error_invitation_changed)
        is HouseholdError.InvitationFailed ->
            TextResource.Resource(Res.string.management_error_invitation_failed)
        is HouseholdError.ShareUnavailable ->
            TextResource.Resource(Res.string.management_error_share)

        is HouseholdError.InvitationRateLimited ->
            TextResource.Resource(Res.string.error_invitation_rate_limited)
        is HouseholdError.InvalidInvitation ->
            TextResource.Resource(Res.string.error_invalid_invitation)
        is HouseholdError.InvalidName -> TextResource.Resource(Res.string.error_invalid_name)
        is HouseholdError.UnsupportedCurrency ->
            TextResource.Resource(Res.string.error_unsupported_currency)
        is HouseholdError.ExpiredInvitation ->
            TextResource.Resource(Res.string.error_expired_invitation)
        is HouseholdError.AlreadyJoined -> TextResource.Resource(Res.string.error_already_joined)
        is HouseholdError.HouseholdFull -> TextResource.Resource(Res.string.error_household_full)
        is HouseholdError.OwnerRequired -> TextResource.Resource(Res.string.error_owner_required)
        else -> toTextResource()
    }
