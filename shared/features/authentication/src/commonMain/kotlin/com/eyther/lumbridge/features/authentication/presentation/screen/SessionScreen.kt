package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.authentication.presentation.components.SessionStartup
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain

/** Hosts supply navigation graphs; this feature owns session rendering and restoration events. */
@Composable
fun SessionScreen(
    viewModel: SessionScreenViewModelContract,
    signedOutContent: @Composable () -> Unit,
    authenticatedContent:
        @Composable
        (String, HouseholdContextDomain?, Boolean, TextResource?) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerSessionScreen(
        stage = state.stage,
        userId = state.userId,
        context = state.context,
        reconnecting = state.reconnecting,
        restoreError = state.restoreError,
        selectionError = state.selectionError ?: state.accessError,
        selectionMessage = state.selectionMessage,
        signingOut = state.signingOut,
        signOutError = state.signOutError,
        onRetry = viewModel::onRetryHouseholdRestore,
        onChooseHousehold = viewModel::onChooseHousehold,
        onDismissSelectionError = viewModel::onDismissSelectionError,
        signedOutContent = signedOutContent,
        authenticatedContent = authenticatedContent,
    )
}

@Composable
private fun InnerSessionScreen(
    stage: SessionStageUi,
    userId: String?,
    context: HouseholdContextDomain?,
    reconnecting: Boolean,
    restoreError: TextResource?,
    selectionError: TextResource?,
    selectionMessage: TextResource?,
    signingOut: Boolean,
    signOutError: TextResource?,
    onRetry: () -> Unit,
    onChooseHousehold: () -> Unit,
    onDismissSelectionError: () -> Unit,
    signedOutContent: @Composable () -> Unit,
    authenticatedContent:
        @Composable
        (String, HouseholdContextDomain?, Boolean, TextResource?) -> Unit,
) {
    when (stage) {
        SessionStageUi.RESTORING -> SessionStartup(reconnecting = reconnecting)
        SessionStageUi.HOUSEHOLD_RESTORE_FAILED ->
            SessionStartup(
                reconnecting = reconnecting,
                error = restoreError,
                onRetry = onRetry,
                onChooseHousehold = onChooseHousehold,
            )
        SessionStageUi.SIGNED_OUT -> signedOutContent()
        SessionStageUi.READY ->
            userId?.let { id ->
                Box(Modifier.fillMaxSize()) {
                    authenticatedContent(id, context, signingOut, signOutError)
                    SuccessSnackbar(
                        message = selectionMessage,
                        modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding(),
                    )
                    selectionError?.let { message ->
                        Box(
                            Modifier.align(Alignment.BottomCenter)
                                .safeDrawingPadding()
                                .padding(LoomSpacing.content),
                        ) {
                            ErrorNotice(message = message, onDismiss = onDismissSelectionError)
                        }
                    }
                }
            }
    }
}
