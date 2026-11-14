package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class CreateHouseholdState(
    val name: String = "",
    val currency: String = "EUR",
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: CreateHouseholdScreenViewModelContract.RedirectTo? = null,
) {

    val canSubmit: Boolean = name.isNotBlank()
}
