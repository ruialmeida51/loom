package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.create_household_description
import com.eyther.lumbridge.features.household.generated.resources.ui_create_household
import com.eyther.lumbridge.features.household.generated.resources.ui_household_currency
import com.eyther.lumbridge.features.household.generated.resources.ui_household_name
import com.eyther.lumbridge.features.household.presentation.components.HouseholdFormLayout
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateHouseholdScreen(
    viewModel: CreateHouseholdScreenViewModelContract,
    onCreated: (HouseholdDomain) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    HandleRedirects(state.redirectTo, onCreated, viewModel::onRedirectHandled)
    InnerCreateHouseholdScreen(
        name = state.name,
        currency = state.currency,
        busy = state.busy,
        canSubmit = state.canSubmit,
        error = state.error,
        onNameChanged = viewModel::onNameChanged,
        onCurrencyChanged = viewModel::onCurrencyChanged,
        onSubmit = viewModel::onSubmit,
        onDismissError = viewModel::onDismissError,
        onBack = onBack,
    )
}

@Composable
private fun InnerCreateHouseholdScreen(
    name: String,
    currency: String,
    busy: Boolean,
    canSubmit: Boolean,
    error: TextResource?,
    onNameChanged: (String) -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    HouseholdFormLayout(title = stringResource(Res.string.ui_create_household), onBack = onBack) {
        Text(stringResource(Res.string.create_household_description))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(Res.string.ui_household_name)) },
            singleLine = true,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(Res.string.ui_household_currency))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            HouseholdCurrencyDomain.supportedCodes.forEach { code ->
                FilterChip(
                    selected = currency == code,
                    onClick = { onCurrencyChanged(code) },
                    label = { Text(code) },
                    enabled = !busy,
                )
            }
        }
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismissError)
        }
        LoomButton(
            text = stringResource(Res.string.ui_create_household),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().testTag("create_household_submit"),
            loading = busy,
            enabled = canSubmit,
        )
    }
}

@Composable
private fun HandleRedirects(
    redirectTo: CreateHouseholdScreenViewModelContract.RedirectTo?,
    onCreated: (HouseholdDomain) -> Unit,
    onHandled: () -> Unit,
) {
    LaunchedEffect(redirectTo) {
        val destination = redirectTo ?: return@LaunchedEffect
        onHandled()
        when (destination) {
            is CreateHouseholdScreenViewModelContract.RedirectTo.OpenHousehold -> onCreated(destination.household)
        }
    }
}
