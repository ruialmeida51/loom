package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.model.HomeDestination
import com.eyther.lumbridge.app.navigation.sheet.TasksSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onRootChanged: (Boolean) -> Unit,
    onShopping: () -> Unit,
    onTasks: () -> Unit,
    onMoney: () -> Unit,
) {
    val state =
        rememberNavigationState(startRoot = HomeDestination.Home) {
            subclass(HomeDestination.Home::class)
            subclass(HomeDestination.Feed::class)
            subclass(HomeDestination.LegacyReminders::class)
            subclass(HomeDestination.ExpenseForm::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val quickTaskViewModel: TasksScreenViewModelContract = koinViewModel()
    val destination = state.getActiveBackStack()?.lastOrNull()
    LaunchedEffect(destination) { onRootChanged(destination == HomeDestination.Home) }
    val uriHandler = LocalUriHandler.current
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { key ->
        when (key) {
            HomeDestination.Home ->
                Scaffold(topBar = portalToolbar) { padding ->
                    Box(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
                        TasksSheetHost(
                            viewModel = quickTaskViewModel,
                            timeZone = context.timeZone,
                        ) {
                            HomeScreen(
                                viewModel = koinViewModel<HomeScreenViewModelContract>(),
                                onShopping = onShopping,
                                onFeed = { navigator.navigateTo(HomeDestination.Feed) },
                                onOpenStory = uriHandler::openUri,
                                onTasks = onTasks,
                                onBalances = onMoney,
                                onAddExpense = {
                                    navigator.navigateTo(
                                        HomeDestination.ExpenseForm(accountId = context.id),
                                    )
                                },
                                onAddTask = {
                                    quickTaskViewModel.onAddSheetVisibilityChanged(true)
                                },
                            )
                        }
                    }
                }
            HomeDestination.Feed -> FeedHost(onBack = navigator::goBack)
            is HomeDestination.LegacyReminders ->
                LaunchedEffect(key) {
                    navigator.resetTo(HomeDestination.Home)
                    if (key.compose) quickTaskViewModel.onAddSheetVisibilityChanged(true)
                    else onTasks()
                }
            is HomeDestination.ExpenseForm ->
                ExpenseFormHost(
                    accountId = key.accountId,
                    expenseId = null,
                    onBack = navigator::goBack,
                    onSaved = navigator::goBack,
                )
        }
    }
}
