package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.model.FeedDestination
import com.eyther.lumbridge.app.navigation.sheet.FeedSheetHost
import com.eyther.lumbridge.app.navigation.sheet.FeedSubscriptionsSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.screen.FeedSubscriptionsScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_feed
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_feed_subscriptions
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Both Home and More open the same feed workflow, returning to their own parent. */
@Composable
internal fun FeedHost(onBack: () -> Unit) {
    val state =
        rememberNavigationState(startRoot = FeedDestination.Articles) {
            subclass(FeedDestination.Articles::class)
            subclass(FeedDestination.Subscriptions::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val feedViewModel: FeedScreenViewModelContract = koinViewModel()
    val destination = state.getActiveBackStack()?.lastOrNull()
    var wasManaging by remember { mutableStateOf(false) }
    LaunchedEffect(destination) {
        if (destination == FeedDestination.Articles && wasManaging) feedViewModel.onSourcesChanged()
        wasManaging = destination == FeedDestination.Subscriptions
    }
    val uriHandler = LocalUriHandler.current
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { destination ->
        when (destination) {
            FeedDestination.Articles -> {
                FlowScaffold(title = stringResource(Res.string.toolbar_feed), onBack = onBack) {
                    FeedSheetHost(viewModel = feedViewModel) {
                        FeedScreen(
                            viewModel = feedViewModel,
                            onOpen = uriHandler::openUri,
                            onManageFeeds = { navigator.navigateTo(FeedDestination.Subscriptions) },
                        )
                    }
                }
            }
            FeedDestination.Subscriptions -> {
                val viewModel: FeedSubscriptionsScreenViewModelContract = koinViewModel()
                FlowScaffold(
                    title =
                        stringResource(
                            Res.string.toolbar_feed_subscriptions,
                        ),
                    onBack = navigator::goBack,
                ) {
                    FeedSubscriptionsSheetHost(viewModel = viewModel) {
                        FeedSubscriptionsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
