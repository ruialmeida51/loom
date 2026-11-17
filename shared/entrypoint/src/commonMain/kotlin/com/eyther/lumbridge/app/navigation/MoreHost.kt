package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.MoreOverviewContent
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.MoreDestination
import com.eyther.lumbridge.app.navigation.sheet.HouseholdDetailsSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdDetailsScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.household_details
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MoreHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onRootChanged: (Boolean) -> Unit,
    onTasks: () -> Unit,
) {
    val state =
        rememberNavigationState(startRoot = MoreDestination.Overview) {
            subclass(MoreDestination.Overview::class)
            subclass(MoreDestination.LegacyReminders::class)
            subclass(MoreDestination.Feed::class)
            subclass(MoreDestination.HouseholdDetails::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val destination = state.getActiveBackStack()?.lastOrNull()
    LaunchedEffect(destination) { onRootChanged(destination == MoreDestination.Overview) }
    ScreenAnalyticsEffect(
        screen =
            when (destination) {
                MoreDestination.Overview -> AnalyticsScreenDomain.MORE
                MoreDestination.LegacyReminders -> AnalyticsScreenDomain.TASKS
                MoreDestination.Feed -> AnalyticsScreenDomain.FEED
                MoreDestination.HouseholdDetails -> AnalyticsScreenDomain.HOUSEHOLD_DETAILS
                else -> null
            },
    )
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { key ->
        when (key) {
            MoreDestination.Overview ->
                Scaffold(topBar = portalToolbar) { padding ->
                    Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
                        MoreOverviewContent(
                            onFeed = { navigator.navigateTo(MoreDestination.Feed) },
                            onHouseholdDetails = {
                                navigator.navigateTo(MoreDestination.HouseholdDetails)
                            },
                        )
                    }
                }
            MoreDestination.Feed -> FeedHost(onBack = navigator::goBack)
            MoreDestination.LegacyReminders ->
                LaunchedEffect(Unit) {
                    navigator.resetTo(MoreDestination.Overview)
                    onTasks()
                }
            MoreDestination.HouseholdDetails -> {
                val viewModel: HouseholdDetailsScreenViewModelContract = koinViewModel()
                FlowScaffold(
                    title = stringResource(Res.string.household_details),
                    onBack = navigator::goBack,
                ) {
                    HouseholdDetailsSheetHost(viewModel = viewModel) {
                        HouseholdDetailsScreen(viewModel = viewModel, context = context)
                    }
                }
            }
        }
    }
}
