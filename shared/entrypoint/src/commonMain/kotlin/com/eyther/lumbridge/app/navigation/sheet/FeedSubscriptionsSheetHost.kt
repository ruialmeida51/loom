package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.FeedSubscriptionSheets
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.feed.presentation.components.bottomsheet.FeedSubscriptionFormBottomSheet
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsScreenViewModelContract

@Composable
internal fun FeedSubscriptionsSheetHost(
    viewModel: FeedSubscriptionsScreenViewModelContract,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    LaunchedEffect(state.showForm) {
        if (state.showForm) controller.showBottomSheet(FeedSubscriptionSheets.Form)
        else controller.hideBottomSheet()
    }
    controller.registerBottomSheet<FeedSubscriptionSheets.Form> {
        FeedSubscriptionFormBottomSheet(
            url = state.url,
            editing = state.editing,
            busy = state.busy,
            canSave = state.canSave,
            error = state.formError,
            onUrl = viewModel::onUrlChanged,
            onSave = viewModel::onSaveFeed,
            onClose = viewModel::onCloseForm,
            onDismissError = viewModel::onDismissError,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = viewModel::onCloseForm,
        content = content,
    )
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            confirmationTag = "confirm_remove_feed",
            onConfirm = viewModel::onConfirmRemoval,
            onDismiss = viewModel::onDismissRemoval,
        )
    }
}
