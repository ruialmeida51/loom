package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.sheet.FeedSheetHost
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Both Home and More open the same feed workflow, returning to their own parent. */
@Composable
internal fun FeedHost(onBack: () -> Unit) {
    val viewModel: FeedScreenViewModelContract = koinViewModel()
    val uriHandler = LocalUriHandler.current
    FlowScaffold(title = stringResource(Res.string.ui_feed), onBack = onBack) {
        FeedSheetHost(viewModel = viewModel) {
            FeedScreen(viewModel = viewModel, onOpen = uriHandler::openUri)
        }
    }
}
