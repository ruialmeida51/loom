package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_money
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_more
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_tasks
import com.eyther.lumbridge.shared.entrypoint.generated.resources.token_home
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_shopping
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onHouseholdExited: (String, Boolean) -> Unit,
) {
    val roots =
        setOf(
            HouseholdDestination.Home,
            HouseholdDestination.Shopping,
            HouseholdDestination.Balances,
            HouseholdDestination.Tasks,
            HouseholdDestination.More,
        )
    val state =
        rememberNavigationState(startRoot = HouseholdDestination.Home, roots = roots) {
            subclass(HouseholdDestination.Home::class)
            subclass(HouseholdDestination.Shopping::class)
            subclass(HouseholdDestination.Balances::class)
            subclass(HouseholdDestination.Tasks::class)
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
            else if (target.route == "tasks" || target.route == "reminders") {
                navigator.navigateTo(HouseholdDestination.Tasks)
                PendingPush.clear()
            } else navigator.navigateTo(HouseholdDestination.Home)
        }
    }
    Scaffold(
        bottomBar = {
            if (showTabs)
                BoxWithConstraints {
                    val tabWidth = maxWidth / roots.size
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(
                        modifier = Modifier.padding(top = 1.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                                    stringResource(Res.string.tab_money),
                                    Glyph.MONEY,
                                ),
                                Triple(
                                    HouseholdDestination.Tasks,
                                    stringResource(Res.string.tab_tasks),
                                    Glyph.TASKS,
                                ),
                                Triple(
                                    HouseholdDestination.More,
                                    stringResource(Res.string.tab_more),
                                    Glyph.MORE,
                                ),
                            )
                            .forEach { (destination, label, glyph) ->
                                val selected = state.activeRoot == destination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigator.navigateTo(destination) },
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            indicatorColor =
                                                MaterialTheme.colorScheme.primaryContainer,
                                            selectedTextColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer,
                                            unselectedTextColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                    icon = {
                                        LoomIcon(
                                            glyph = glyph,
                                            tint =
                                                if (selected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight =
                                                if (selected) FontWeight.SemiBold
                                                else FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            // Use the full tab width before truncating accessible
                                            // larger text.
                                            modifier =
                                                Modifier.wrapContentWidth(unbounded = true)
                                                    .widthIn(max = tabWidth),
                                        )
                                    },
                                    modifier =
                                        Modifier.testTag("navigation_${glyph.name.lowercase()}"),
                                )
                            }
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
                        onShopping = { navigator.navigateTo(HouseholdDestination.Shopping) },
                        onTasks = { navigator.navigateTo(HouseholdDestination.Tasks) },
                        onMoney = { navigator.navigateTo(HouseholdDestination.Balances) },
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
                HouseholdDestination.Tasks ->
                    TasksHost(
                        context = context,
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                    )
                HouseholdDestination.More ->
                    MoreHost(
                        onHouseholdExited = { explicit -> onHouseholdExited(context.id, explicit) },
                        context = context,
                        portalToolbar = portalToolbar,
                        onRootChanged = { showTabs = it },
                        onTasks = { navigator.navigateTo(HouseholdDestination.Tasks) },
                    )
            }
        }
    }
}
