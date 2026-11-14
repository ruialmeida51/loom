package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.join_household_description
import com.eyther.lumbridge.features.household.generated.resources.ui_invitation_code
import com.eyther.lumbridge.features.household.generated.resources.ui_join_household
import com.eyther.lumbridge.features.household.presentation.components.HouseholdFormLayout
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinHouseholdScreen(viewModel: JoinHouseholdScreenViewModelContract, onJoined: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    HandleRedirects(state.redirectTo, onJoined, viewModel::onRedirectHandled)
    InnerJoinHouseholdScreen(
        token = state.token,
        busy = state.busy,
        canSubmit = state.canSubmit,
        error = state.error,
        onTokenChanged = viewModel::onInvitationCodeChanged,
        onSubmit = viewModel::onSubmit,
        onDismissError = viewModel::onDismissError,
        onBack = onBack,
    )
}

@Composable
private fun InnerJoinHouseholdScreen(
    token: String,
    busy: Boolean,
    canSubmit: Boolean,
    error: TextResource?,
    onTokenChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    HouseholdFormLayout(title = stringResource(Res.string.ui_join_household), onBack = onBack) {
        Text(stringResource(Res.string.join_household_description))
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChanged,
            label = { Text(stringResource(Res.string.ui_invitation_code)) },
            singleLine = true,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismissError)
        }
        LoomButton(
            text = stringResource(Res.string.ui_join_household),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().testTag("join_household_submit"),
            loading = busy,
            enabled = canSubmit,
        )
    }
}

@Composable
private fun HandleRedirects(
    redirectTo: JoinHouseholdScreenViewModelContract.RedirectTo?,
    onJoined: () -> Unit,
    onHandled: () -> Unit,
) {
    LaunchedEffect(redirectTo) {
        val destination = redirectTo ?: return@LaunchedEffect
        onHandled()
        when (destination) {
            JoinHouseholdScreenViewModelContract.RedirectTo.Households -> onJoined()
        }
    }
}
