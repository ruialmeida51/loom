package com.eyther.lumbridge.features.household.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class HouseholdMemberUi(
    val id: String,
    val name: String,
    val initials: String,
    val isOwner: Boolean,
    val isCurrentUser: Boolean,
    val canManage: Boolean = false,
)
