package com.eyther.lumbridge.features.household.presentation.model

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class HouseholdConfirmationContentUi(
    val title: TextResource,
    val message: TextResource,
    val action: TextResource,
    val destructive: Boolean,
    val balanceMemberId: String? = null,
    val requiresName: Boolean = false,
)
