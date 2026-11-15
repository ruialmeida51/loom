package com.eyther.lumbridge.app.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.demo.data.DemoAnalyticsDataSource
import com.eyther.lumbridge.app.demo.data.DemoExpenseAccountDataSource
import com.eyther.lumbridge.app.demo.data.DemoExpenseDataSource
import com.eyther.lumbridge.app.demo.data.DemoFeedDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoProfileDataSource
import com.eyther.lumbridge.app.demo.data.DemoRecurringExpenseDataSource
import com.eyther.lumbridge.app.demo.data.DemoReminderDataSource
import com.eyther.lumbridge.app.demo.data.DemoSettingsDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingSelectionDataSource
import com.eyther.lumbridge.app.demo.data.DemoStatementDataSource
import com.eyther.lumbridge.app.navigation.AccountHost
import com.eyther.lumbridge.app.navigation.HouseholdHost
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.PortalToolbar
import com.eyther.lumbridge.app.navigation.model.PortalDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.core.navigation.composables.navigation3.DefaultNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.di.providedAccountModules
import com.eyther.lumbridge.di.providedHouseholdModules
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.insights.data.export.CommonStatementExportDataSource
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_choose_a_preview_household
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

@Composable
internal fun DemoPortalHost(
    stores: Map<String, DemoHouseholdStore>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    val settings = remember { DemoSettingsDataSource() }
    val analytics = remember { DemoAnalyticsDataSource() }
    val profile = remember(stores) { DemoProfileDataSource(stores = stores.values) }
    val accountGraph = remember {
        koinApplication {
            modules(
                providedAccountModules(
                    profileDataSource = profile,
                    householdDataSource = DemoHouseholdDataSource(store = stores.values.first()),
                    analyticsDataSource = analytics,
                    settingsDataSource = settings,
                ),
            )
        }
    }
    DisposableEffect(accountGraph) { onDispose { accountGraph.close() } }
    val store = stores.getValue(selectedId)
    val householdGraph =
        remember(store) {
            koinApplication {
                modules(
                    providedHouseholdModules(
                        context = store.context,
                        householdDataSource = DemoHouseholdDataSource(store = store),
                        analyticsDataSource = analytics,
                        shoppingDataSource = DemoShoppingDataSource(store = store),
                        shoppingSelectionDataSource =
                            DemoShoppingSelectionDataSource(store = store),
                        reminderDataSource = DemoReminderDataSource(store = store),
                        expenseAccountDataSource = DemoExpenseAccountDataSource(store = store),
                        expenseDataSource = DemoExpenseDataSource(store = store),
                        recurringExpenseDataSource = DemoRecurringExpenseDataSource(store = store),
                        feedDataSource = DemoFeedDataSource(store = store),
                        statementDataSource = DemoStatementDataSource(),
                        statementExportDataSource = CommonStatementExportDataSource(),
                        settingsDataSource = settings,
                    ),
                )
            }
        }
    DisposableEffect(householdGraph) { onDispose { householdGraph.close() } }
    val accountViewModelScope = rememberHostViewModelStoreOwner()
    KoinIsolatedContext(context = accountGraph) {
        val accountViewModel: AccountScreenViewModelContract =
            koinViewModel(key = "preview-account", viewModelStoreOwner = accountViewModelScope) {
                parametersOf(
                    AccountScreenViewModelContract.Params(userId = DemoHouseholdStore.USER),
                )
            }
        val navigationState =
            rememberNavigationState(startRoot = PortalDestination.Household) {
                subclass(PortalDestination.Household::class)
                subclass(PortalDestination.Households::class)
                subclass(PortalDestination.Account::class)
            }
        val navigator = remember(navigationState) { Navigator(state = navigationState) }
        val pendingPush by PendingPush.destination.collectAsState()
        LaunchedEffect(pendingPush, store.context.id) {
            if (pendingPush != null && pendingPush?.householdId == store.context.id) {
                navigator.navigateAndPopTo(PortalDestination.Household)
            }
        }
        val portalToolbar: @Composable () -> Unit = {
            val accountState by accountViewModel.state.collectAsState()
            PortalToolbar(
                householdName = store.context.name,
                nickname = accountState.profile?.nickname.orEmpty(),
                onHousehold = { navigator.navigateTo(PortalDestination.Households) },
                onAccount = { navigator.navigateTo(PortalDestination.Account) },
            )
        }
        DefaultNavDisplay(navigator = navigator, navigationState = navigationState) { destination ->
            when (destination) {
                PortalDestination.Household ->
                    key(selectedId) {
                        KoinIsolatedContext(context = householdGraph) {
                            HouseholdHost(context = store.context, portalToolbar = portalToolbar)
                        }
                    }
                PortalDestination.Account ->
                    AccountHost(
                        viewModel = accountViewModel,
                        onBack = navigator::goBack,
                        onSignOut = onSignOut,
                    )
                PortalDestination.Households ->
                    FlowScaffold(
                        title = stringResource(Res.string.ui_choose_a_preview_household),
                        onBack = navigator::goBack,
                    ) {
                        Column {
                            stores.values.forEach { household ->
                                TextButton(
                                    onClick = {
                                        onSelect(household.context.id)
                                        navigator.goBack()
                                    },
                                ) {
                                    Text(household.context.name)
                                }
                            }
                        }
                    }
            }
        }
    }
}
