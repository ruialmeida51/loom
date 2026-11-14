package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.navigation.components.HouseholdToolbar
import com.eyther.lumbridge.app.navigation.model.HouseholdDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.navigation.sheet.ExpensesSheetHost
import com.eyther.lumbridge.app.navigation.sheet.FeedSheetHost
import com.eyther.lumbridge.app.navigation.sheet.RemindersSheetHost
import com.eyther.lumbridge.app.navigation.sheet.SettingsSheetHost
import com.eyther.lumbridge.app.navigation.sheet.ShoppingSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.RecurringExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.presentation.screen.InsightsScreen
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import com.eyther.lumbridge.features.reminders.presentation.screen.RemindersScreen
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.presentation.screen.SettingsScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.screen.ShoppingScreen
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.token_home
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_money
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_shopping
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HouseholdHost(
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
) {
    val getLegalDocumentsUseCase: GetLegalDocumentsUseCase = koinInject()
    val preferencesUseCase: GetSettingsStreamUseCase = koinInject()
    val preferences by preferencesUseCase().collectAsState()
    val mode = runCatching { ThemeMode.valueOf(preferences.theme) }.getOrDefault(ThemeMode.SYSTEM)
    LoomTheme(mode) {
        val roots =
            setOf(
                HouseholdDestination.Home,
                HouseholdDestination.Feed,
                HouseholdDestination.Shopping,
                HouseholdDestination.Money,
            )
        val state =
            rememberNavigationState(startRoot = HouseholdDestination.Home, roots = roots) {
                subclass(HouseholdDestination.Home::class)
                subclass(HouseholdDestination.Feed::class)
                subclass(HouseholdDestination.Shopping::class)
                subclass(HouseholdDestination.Money::class)
                subclass(HouseholdDestination.Reminders::class)
                subclass(HouseholdDestination.RecurringExpenses::class)
                subclass(HouseholdDestination.Insights::class)
                subclass(HouseholdDestination.Settings::class)
            }
        val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase = koinInject()
        val getAnalyticsPreferencesStreamUseCase: GetAnalyticsPreferencesStreamUseCase =
            koinInject()
        val analyticsPreferences by getAnalyticsPreferencesStreamUseCase().collectAsState()
        val activeDestination = state.getActiveBackStack()?.lastOrNull()
        LaunchedEffect(activeDestination, analyticsPreferences.enabled) {
            val screen =
                when (activeDestination) {
                    HouseholdDestination.Home -> AnalyticsScreenDomain.HOME
                    HouseholdDestination.Feed -> AnalyticsScreenDomain.FEED
                    HouseholdDestination.Shopping -> AnalyticsScreenDomain.SHOPPING
                    HouseholdDestination.Money -> AnalyticsScreenDomain.EXPENSES
                    HouseholdDestination.Reminders -> AnalyticsScreenDomain.REMINDERS
                    HouseholdDestination.RecurringExpenses ->
                        AnalyticsScreenDomain.RECURRING_EXPENSES
                    HouseholdDestination.Insights -> AnalyticsScreenDomain.INSIGHTS
                    HouseholdDestination.Settings -> AnalyticsScreenDomain.SETTINGS
                    else -> null
                }
            if (screen != null) {
                trackAnalyticsEventUseCase(
                    event = AnalyticsEventDomain.ScreenViewed(screen = screen),
                )
            }
        }
        val navigator = remember(state) { Navigator(state) }
        val pendingPush by PendingPush.destination.collectAsState()
        LaunchedEffect(pendingPush, context.id) {
            val target = pendingPush
            if (target != null && target.householdId == context.id) {
                PendingPush.clear()
                navigator.navigateTo(
                    when (target.route) {
                        "money" -> HouseholdDestination.Money
                        "insights" -> HouseholdDestination.Insights
                        else -> HouseholdDestination.Reminders
                    },
                )
            }
        }
        val uriHandler = LocalUriHandler.current
        Scaffold(
            topBar = {
                Column {
                    HouseholdToolbar(
                        destination =
                            activeDestination as? HouseholdDestination ?: HouseholdDestination.Home,
                        context = context,
                        showBackButton = (state.getActiveBackStack()?.size ?: 0) > 1,
                        onBack = navigator::goBack,
                        onSwitchHousehold = onSwitchHousehold,
                        onOpenReminders = { navigator.navigateTo(HouseholdDestination.Reminders) },
                        onOpenSettings = { navigator.navigateTo(HouseholdDestination.Settings) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    val tabs =
                        listOf(
                            Triple(
                                HouseholdDestination.Home,
                                stringResource(Res.string.token_home),
                                Glyph.HOME,
                            ),
                            Triple(
                                HouseholdDestination.Feed,
                                stringResource(Res.string.ui_feed),
                                Glyph.FEED,
                            ),
                            Triple(
                                HouseholdDestination.Shopping,
                                stringResource(Res.string.ui_shopping),
                                Glyph.SHOPPING,
                            ),
                            Triple(
                                HouseholdDestination.Money,
                                stringResource(Res.string.ui_money),
                                Glyph.MONEY,
                            ),
                        )
                    tabs.forEach { (destination, label, glyph) ->
                        NavigationBarItem(
                            modifier = Modifier.testTag("navigation_${glyph.name.lowercase()}"),
                            selected = state.activeRoot == destination,
                            onClick = { navigator.navigateTo(destination) },
                            icon = {
                                LoomIcon(
                                    glyph,
                                    tint =
                                        if (state.activeRoot == destination)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            label = { Text(label) },
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            HorizontalSlideNavDisplay(
                Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize(),
                navigator,
                state,
            ) { destination ->
                when (destination) {
                    HouseholdDestination.Home ->
                        HomeScreen(
                            koinViewModel<HomeScreenViewModelContract>(),
                            context,
                            { navigator.navigateTo(HouseholdDestination.Shopping) },
                            { navigator.navigateTo(HouseholdDestination.Reminders) },
                            { navigator.navigateTo(HouseholdDestination.Money) },
                            { navigator.navigateTo(HouseholdDestination.Feed) },
                        )
                    HouseholdDestination.Feed -> {
                        val viewModel: FeedScreenViewModelContract = koinViewModel()
                        FeedSheetHost(viewModel = viewModel) {
                            FeedScreen(viewModel = viewModel, onOpen = uriHandler::openUri)
                        }
                    }
                    HouseholdDestination.Shopping -> {
                        val viewModel: ShoppingScreenViewModelContract = koinViewModel()
                        ShoppingSheetHost(viewModel = viewModel) {
                            ShoppingScreen(viewModel = viewModel)
                        }
                    }
                    HouseholdDestination.Money -> {
                        val viewModel: ExpensesScreenViewModelContract = koinViewModel()
                        ExpensesSheetHost(viewModel = viewModel, context = context) {
                            ExpensesScreen(
                                viewModel = viewModel,
                                context = context,
                                onInsights = {
                                    navigator.navigateTo(HouseholdDestination.Insights)
                                },
                                onRecurringExpenses = {
                                    navigator.navigateTo(HouseholdDestination.RecurringExpenses)
                                },
                            )
                        }
                    }
                    HouseholdDestination.Reminders -> {
                        val viewModel: RemindersScreenViewModelContract = koinViewModel()
                        RemindersSheetHost(viewModel = viewModel, timeZone = context.timeZone) {
                            RemindersScreen(viewModel = viewModel, timeZone = context.timeZone)
                        }
                    }
                    HouseholdDestination.RecurringExpenses ->
                        RecurringExpensesScreen(
                            koinViewModel<RecurringExpensesScreenViewModelContract>(),
                            context,
                        )
                    HouseholdDestination.Insights ->
                        InsightsScreen(
                            viewModel = koinViewModel<InsightsScreenViewModelContract>(),
                            context = context,
                        )
                    HouseholdDestination.Settings -> {
                        val viewModel: SettingsScreenViewModelContract = koinViewModel()
                        SettingsSheetHost(viewModel = viewModel) {
                            SettingsScreen(
                                viewModel = viewModel,
                                context = context,
                                onSwitchHousehold = onSwitchHousehold,
                                onSignOut = onSignOut,
                                onOpenLegalDocument = { document ->
                                    uriHandler.openUri(
                                        getLegalDocumentsUseCase().url(document = document),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
