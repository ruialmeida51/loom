package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.navigation.components.PortalToolbar
import com.eyther.lumbridge.app.navigation.model.PortalDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.navigation.composables.navigation3.DefaultNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.di.accountModules
import com.eyther.lumbridge.di.householdModules
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import kotlinx.serialization.modules.subclass
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

/** The user graph outlives household routes, including users who have not joined a household. */
@Composable
internal fun PortalHost(
    userId: String,
    context: HouseholdContextDomain?,
    access: SupabaseAccess,
    householdDataSource: HouseholdDataSource,
    profileDataSource: ProfileDataSource,
    analyticsDataSource: AnalyticsDataSource,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
) {
    val accountGraph =
        remember(userId) {
            koinApplication {
                modules(
                    accountModules(
                        userId = userId,
                        access = access,
                        profileDataSource = profileDataSource,
                        householdDataSource = householdDataSource,
                        analyticsDataSource = analyticsDataSource,
                    ),
                )
            }
        }
    DisposableEffect(accountGraph) { onDispose { accountGraph.close() } }
    val householdGraph =
        remember(context) {
            context?.let { selected ->
                koinApplication {
                    modules(
                        householdModules(
                            context = selected,
                            access = access,
                            householdDataSource = householdDataSource,
                            analyticsDataSource = analyticsDataSource,
                            settingsDataSource = accountGraph.koin.get<SettingsDataSource>(),
                        ),
                    )
                }
            }
        }
    DisposableEffect(householdGraph) { onDispose { householdGraph?.close() } }
    val accountViewModelScope = rememberHostViewModelStoreOwner()
    KoinIsolatedContext(context = accountGraph) {
        val accountViewModel: AccountScreenViewModelContract =
            koinViewModel(key = "account-$userId", viewModelStoreOwner = accountViewModelScope) {
                parametersOf(AccountScreenViewModelContract.Params(userId = userId))
            }
        val state =
            rememberNavigationState(startRoot = PortalDestination.Household) {
                subclass(PortalDestination.Household::class)
                subclass(PortalDestination.Households::class)
                subclass(PortalDestination.Account::class)
            }
        val navigator = remember(state) { Navigator(state = state) }
        val pendingPush by PendingPush.destination.collectAsState()
        LaunchedEffect(pendingPush, context?.id) {
            if (pendingPush != null && pendingPush?.householdId == context?.id) {
                navigator.navigateAndPopTo(PortalDestination.Household)
            }
        }
        val portalToolbar: @Composable () -> Unit = {
            val accountState by accountViewModel.state.collectAsState()
            PortalToolbar(
                householdName = context?.name,
                nickname = accountState.profile?.nickname.orEmpty(),
                onHousehold = { navigator.navigateTo(PortalDestination.Households) },
                onAccount = { navigator.navigateTo(PortalDestination.Account) },
            )
        }
        DefaultNavDisplay(navigator = navigator, navigationState = state) { destination ->
            when (destination) {
                PortalDestination.Household -> {
                    if (context == null || householdGraph == null) {
                        HouseholdSetupHost(
                            userId = userId,
                            onSelect = onSelect,
                            onSignOut = onSignOut,
                            onAccount = { navigator.navigateTo(PortalDestination.Account) },
                        )
                    } else {
                        key(context.id) {
                            KoinIsolatedContext(context = householdGraph) {
                                HouseholdHost(context = context, portalToolbar = portalToolbar)
                            }
                        }
                    }
                }
                PortalDestination.Households ->
                    HouseholdSetupHost(
                        userId = userId,
                        onSelect = { household ->
                            onSelect(household)
                            navigator.goBack()
                        },
                        onSignOut = onSignOut,
                        onBack = navigator::goBack,
                        onAccount = { navigator.navigateTo(PortalDestination.Account) },
                    )
                PortalDestination.Account ->
                    AccountHost(
                        viewModel = accountViewModel,
                        onBack = navigator::goBack,
                        onSignOut = onSignOut,
                    )
            }
        }
    }
}
