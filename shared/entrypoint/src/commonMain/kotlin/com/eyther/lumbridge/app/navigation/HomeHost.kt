package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.HomeDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.navigation.sheet.FeedSheetHost
import com.eyther.lumbridge.app.navigation.sheet.HouseholdDetailsSheetHost
import com.eyther.lumbridge.app.navigation.sheet.RemindersSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdDetailsScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import com.eyther.lumbridge.features.reminders.presentation.screen.RemindersScreen
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.household_details
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_reminders
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onRootChanged: (Boolean) -> Unit,
    onShopping: () -> Unit,
) {
    val state =
        rememberNavigationState(startRoot = HomeDestination.Home) {
            subclass(HomeDestination.Home::class)
            subclass(HomeDestination.Feed::class)
            subclass(HomeDestination.Reminders::class)
            subclass(HomeDestination.Details::class)
            subclass(HomeDestination.ExpenseForm::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val destination = state.getActiveBackStack()?.lastOrNull()
    ScreenAnalyticsEffect(
        screen =
            when (destination) {
                HomeDestination.Home -> AnalyticsScreenDomain.HOME
                HomeDestination.Feed -> AnalyticsScreenDomain.FEED
                is HomeDestination.Reminders -> AnalyticsScreenDomain.REMINDERS
                HomeDestination.Details -> AnalyticsScreenDomain.HOUSEHOLD_DETAILS
                else -> null
            },
    )
    LaunchedEffect(destination) { onRootChanged(destination == HomeDestination.Home) }
    val pendingPush by PendingPush.destination.collectAsState()
    LaunchedEffect(pendingPush) {
        val push = pendingPush
        if (
            push != null &&
                push.householdId == context.id &&
                push.route != "money" &&
                push.route != "insights"
        ) {
            PendingPush.clear()
            navigator.navigateTo(HomeDestination.Reminders())
        }
    }
    val uriHandler = LocalUriHandler.current
    val legalDocuments: GetLegalDocumentsUseCase = koinInject()
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { key ->
        when (key) {
            HomeDestination.Home ->
                Scaffold(topBar = portalToolbar) { padding ->
                    Box(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
                        HomeScreen(
                            viewModel = koinViewModel<HomeScreenViewModelContract>(),
                            context = context,
                            onShopping = onShopping,
                            onReminders = { navigator.navigateTo(HomeDestination.Reminders()) },
                            onMoney = {
                                navigator.navigateTo(
                                    HomeDestination.ExpenseForm(accountId = context.id),
                                )
                            },
                            onAddReminder = {
                                navigator.navigateTo(HomeDestination.Reminders(compose = true))
                            },
                            onFeed = { navigator.navigateTo(HomeDestination.Feed) },
                            onHousehold = { navigator.navigateTo(HomeDestination.Details) },
                        )
                    }
                }
            HomeDestination.Feed -> {
                val viewModel: FeedScreenViewModelContract = koinViewModel()
                FlowScaffold(
                    title = stringResource(Res.string.ui_feed),
                    onBack = navigator::goBack,
                ) {
                    FeedSheetHost(viewModel = viewModel) {
                        FeedScreen(viewModel = viewModel, onOpen = uriHandler::openUri)
                    }
                }
            }
            is HomeDestination.ExpenseForm ->
                ExpenseFormHost(
                    accountId = key.accountId,
                    expenseId = null,
                    onBack = navigator::goBack,
                    onSaved = navigator::goBack,
                )
            is HomeDestination.Reminders -> {
                val viewModel: RemindersScreenViewModelContract = koinViewModel()
                LaunchedEffect(key) { if (key.compose) viewModel.onAddSheetVisibilityChanged(true) }
                FlowScaffold(
                    title = stringResource(Res.string.toolbar_reminders),
                    onBack = navigator::goBack,
                ) {
                    RemindersSheetHost(viewModel = viewModel, timeZone = context.timeZone) {
                        RemindersScreen(viewModel = viewModel, timeZone = context.timeZone)
                    }
                }
            }
            HomeDestination.Details -> {
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
