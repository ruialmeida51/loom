package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.navigation.model.HouseholdDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.RecurringExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.presentation.components.HouseholdBadge
import com.eyther.lumbridge.features.insights.presentation.screen.InsightsScreen
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.reminders.presentation.screen.RemindersScreen
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.presentation.screen.SettingsScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.screen.ShoppingScreen
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.loom_logo
import com.eyther.lumbridge.shared.entrypoint.generated.resources.token_home
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_back
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_loom
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_money
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_our_household
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_shopping
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HouseholdHost(
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
) {
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
                Column(Modifier.statusBarsPadding()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painterResource(Res.drawable.loom_logo),
                            stringResource(Res.string.ui_loom),
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(Res.string.ui_loom),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { navigator.navigateTo(HouseholdDestination.Reminders) },
                        ) {
                            LoomIcon(Glyph.BELL)
                        }
                        IconButton(
                            onClick = { navigator.navigateTo(HouseholdDestination.Settings) },
                        ) {
                            LoomIcon(Glyph.SETTINGS)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.getActiveBackStack()?.size ?: 0 > 1) {
                            TextButton(onClick = navigator::goBack) {
                                Text(stringResource(Res.string.ui_back))
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        TextButton(
                            onClick = onSwitchHousehold,
                            modifier = Modifier.testTag("household_switch"),
                        ) {
                            HouseholdBadge(context.id, context.name)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                context.name + " ⌄",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            stringResource(Res.string.ui_our_household),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
            HorizontalSlideNavDisplay(Modifier.padding(padding).fillMaxSize(), navigator, state) {
                destination ->
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
                    HouseholdDestination.Feed ->
                        FeedScreen(koinViewModel<FeedScreenViewModelContract>()) { url ->
                            if (url.startsWith("https://")) uriHandler.openUri(url)
                        }
                    HouseholdDestination.Shopping ->
                        ShoppingScreen(koinViewModel<ShoppingScreenViewModelContract>())
                    HouseholdDestination.Money ->
                        ExpensesScreen(
                            koinViewModel<ExpensesScreenViewModelContract>(),
                            context,
                            onInsights = { navigator.navigateTo(HouseholdDestination.Insights) },
                            onRecurringExpenses = {
                                navigator.navigateTo(HouseholdDestination.RecurringExpenses)
                            },
                        )
                    HouseholdDestination.Reminders ->
                        RemindersScreen(
                            koinViewModel<RemindersScreenViewModelContract>(),
                            context.timeZone,
                        )
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
                    HouseholdDestination.Settings ->
                        SettingsScreen(
                            koinViewModel<SettingsScreenViewModelContract>(),
                            context,
                            onSwitchHousehold,
                            onSignOut,
                        )
                }
            }
        }
    }
}
