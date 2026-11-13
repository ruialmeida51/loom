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
import com.eyther.lumbridge.features.household.generated.resources.error_owner_required
import com.eyther.lumbridge.features.household.generated.resources.error_unsupported_currency

fun AppError.toHouseholdTextResource(): TextResource = when (this) {
    is HouseholdError.InvalidInvitation -> TextResource.Resource(Res.string.error_invalid_invitation)
    is HouseholdError.InvalidName -> TextResource.Resource(Res.string.error_invalid_name)
    is HouseholdError.UnsupportedCurrency -> TextResource.Resource(Res.string.error_unsupported_currency)
    is HouseholdError.ExpiredInvitation -> TextResource.Resource(Res.string.error_expired_invitation)
    is HouseholdError.AlreadyJoined -> TextResource.Resource(Res.string.error_already_joined)
    is HouseholdError.HouseholdFull -> TextResource.Resource(Res.string.error_household_full)
    is HouseholdError.OwnerRequired -> TextResource.Resource(Res.string.error_owner_required)
    else -> toTextResource()
}
