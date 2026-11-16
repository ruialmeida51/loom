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
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.HomeDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.navigation.sheet.RemindersSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.presentation.screen.RemindersScreen
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_reminders
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
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
            subclass(HomeDestination.Reminders::class)
            subclass(HomeDestination.ExpenseForm::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val destination = state.getActiveBackStack()?.lastOrNull()
    ScreenAnalyticsEffect(
        screen =
            when (destination) {
                HomeDestination.Home -> AnalyticsScreenDomain.HOME
                is HomeDestination.Reminders -> AnalyticsScreenDomain.REMINDERS
                else -> null
            },
    )
    LaunchedEffect(destination) { onRootChanged(destination == HomeDestination.Home) }
    val pendingPush by PendingPush.destination.collectAsState()
    LaunchedEffect(pendingPush) {
        val push = pendingPush
        if (push != null && push.householdId == context.id && push.route == "reminders") {
            PendingPush.clear()
            navigator.navigateTo(HomeDestination.Reminders())
        }
    }
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { key ->
        when (key) {
            HomeDestination.Home ->
                Scaffold(topBar = portalToolbar) { padding ->
                    Box(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
                        HomeScreen(
                            viewModel = koinViewModel<HomeScreenViewModelContract>(),
                            onShopping = onShopping,
                            onReminders = { navigator.navigateTo(HomeDestination.Reminders()) },
                            onAddExpense = {
                                navigator.navigateTo(
                                    HomeDestination.ExpenseForm(accountId = context.id),
                                )
                            },
                            onAddReminder = {
                                navigator.navigateTo(HomeDestination.Reminders(compose = true))
                            },
                        )
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
                        RemindersScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
