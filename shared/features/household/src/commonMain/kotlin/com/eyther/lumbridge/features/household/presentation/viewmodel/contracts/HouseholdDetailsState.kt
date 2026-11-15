package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class HouseholdDetailsState(
    val userId: String,
    val members: List<MemberDomain> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val invitation: String? = null,
    val error: TextResource? = null,
) {
    val canInvite: Boolean = members.firstOrNull { it.id == userId }?.role == "owner" && !busy
}
