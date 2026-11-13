package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.data.PendingPush
import com.eyther.lumbridge.core.data.PlatformExport
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.insights.presentation.screen.InsightsScreen
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.reminders.presentation.screen.RemindersScreen
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.usecase.ObserveSettingsUseCase
import com.eyther.lumbridge.features.settings.presentation.screen.SettingsScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.screen.ShoppingScreen
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.loom_logo
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Serializable
private sealed interface HouseholdDestination : NavDestination {
    @Serializable data object Home : HouseholdDestination

    @Serializable data object Feed : HouseholdDestination

    @Serializable data object Shopping : HouseholdDestination

    @Serializable data object Money : HouseholdDestination

    @Serializable data object Reminders : HouseholdDestination

    @Serializable data object Insights : HouseholdDestination

    @Serializable data object Settings : HouseholdDestination
}

@Composable
fun HouseholdHost(
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
) {
    val preferencesUseCase: ObserveSettingsUseCase = koinInject()
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
                            "Loom",
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Loom", style = MaterialTheme.typography.headlineMedium)
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
                            TextButton(onClick = navigator::goBack) { Text("‹ Back") }
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            context.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "OUR HOUSEHOLD",
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
                            Triple(HouseholdDestination.Home, "Home", Glyph.HOME),
                            Triple(HouseholdDestination.Feed, "Feed", Glyph.FEED),
                            Triple(HouseholdDestination.Shopping, "Shopping", Glyph.SHOPPING),
                            Triple(HouseholdDestination.Money, "Money", Glyph.MONEY),
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
                        ExpensesScreen(koinViewModel<ExpensesScreenViewModelContract>(), context) {
                            navigator.navigateTo(HouseholdDestination.Insights)
                        }
                    HouseholdDestination.Reminders ->
                        RemindersScreen(
                            koinViewModel<RemindersScreenViewModelContract>(),
                            context.timeZone,
                        )
                    HouseholdDestination.Insights ->
                        InsightsScreen(
                            koinViewModel<InsightsScreenViewModelContract>(),
                            context,
                            PlatformExport::shareCsv,
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
