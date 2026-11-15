package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eyther.lumbridge.app.navigation.model.HouseholdDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.core.navigation.composables.navigation3.DefaultNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_balances
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_more
import com.eyther.lumbridge.shared.entrypoint.generated.resources.token_home
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_shopping
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
) {
    val roots =
        setOf(
            HouseholdDestination.Home,
            HouseholdDestination.Shopping,
            HouseholdDestination.Balances,
            HouseholdDestination.More,
        )
    val state =
        rememberNavigationState(startRoot = HouseholdDestination.Home, roots = roots) {
            subclass(HouseholdDestination.Home::class)
            subclass(HouseholdDestination.Shopping::class)
            subclass(HouseholdDestination.Balances::class)
            subclass(HouseholdDestination.More::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    var showTabs by remember { mutableStateOf(true) }
    val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = state.activeRoot != HouseholdDestination.Home && showTabs,
        onBackCompleted = { navigator.navigateTo(HouseholdDestination.Home) },
    )
    val pendingPush by PendingPush.destination.collectAsState()
    LaunchedEffect(pendingPush, context.id) {
        val target = pendingPush
        if (target != null && target.householdId == context.id) {
            if (target.route == "money" || target.route == "insights")
                navigator.navigateTo(HouseholdDestination.Balances)
            else navigator.navigateTo(HouseholdDestination.Home)
        }
    }
    Scaffold(
        bottomBar = {
            if (showTabs)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    listOf(
                            Triple(
                                HouseholdDestination.Home,
                                stringResource(Res.string.token_home),
                                Glyph.HOME,
                            ),
                            Triple(
                                HouseholdDestination.Shopping,
                                stringResource(Res.string.ui_shopping),
                                Glyph.SHOPPING,
                            ),
                            Triple(
                                HouseholdDestination.Balances,
                                stringResource(Res.string.tab_balances),
                                Glyph.MONEY,
                            ),
                            Triple(
                                HouseholdDestination.More,
                                stringResource(Res.string.tab_more),
                                Glyph.MORE,
                            ),
                        )
                        .forEach { (destination, label, glyph) ->
                            NavigationBarItem(
                                selected = state.activeRoot == destination,
                                onClick = { navigator.navigateTo(destination) },
                                icon = { LoomIcon(glyph = glyph) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("navigation_${glyph.name.lowercase()}"),
                            )
                        }
                }
        },
    ) { padding ->
        DefaultNavDisplay(
            modifier = Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize(),
            navigator = navigator,
            navigationState = state,
        ) { destination ->
            when (destination) {
                HouseholdDestination.Home ->
                    HomeHost(
                        context = context,
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                    )
                HouseholdDestination.Shopping ->
                    ShoppingHost(
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                    )
                HouseholdDestination.Balances ->
                    BalancesHost(
                        context = context,
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                    )
                HouseholdDestination.More ->
                    MoreHost(
                        context = context,
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                    )
            }
        }
    }
}
