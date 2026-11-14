package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class JoinHouseholdState(
    val token: String = "",
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: JoinHouseholdScreenViewModelContract.RedirectTo? = null,
) {

    val canSubmit: Boolean = token.isNotBlank()
}
