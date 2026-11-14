package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.FeedSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.feed.presentation.components.bottomsheet.FollowFeedBottomSheet
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract

@Composable
internal fun FeedSheetHost(
    viewModel: FeedScreenViewModelContract,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showFollow -> FeedSheets.FollowFeed
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<FeedSheets.FollowFeed> {
        FollowFeedBottomSheet(
            url = state.url,
            busy = state.busy,
            formError = state.formError,
            canFollow = state.canFollow,
            onUrl = viewModel::onUrlChanged,
            onShowFollow = viewModel::onFollowSheetVisibilityChanged,
            onDismiss = viewModel::onDismissError,
            onFollow = viewModel::onFollowFeed,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = { viewModel.onFollowSheetVisibilityChanged(false) },
        content = content,
    )
}
