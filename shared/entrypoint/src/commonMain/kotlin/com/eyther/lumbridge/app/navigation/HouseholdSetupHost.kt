package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.HouseholdSetupDestination
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.presentation.screen.CreateHouseholdScreen
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdScreen
import com.eyther.lumbridge.features.household.presentation.screen.JoinHouseholdScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_households
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HouseholdSetupHost(
    userId: String,
    onSelect: (HouseholdDomain) -> Unit,
    onBack: (() -> Unit)? = null,
    onAccount: (() -> Unit)? = null,
) {
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.HOUSEHOLDS)
    val selectionViewModel: HouseholdScreenViewModelContract =
        koinViewModel(key = "households-$userId")
    val state =
        rememberNavigationState(
            startRoot = HouseholdSetupDestination.Select,
            roots = setOf(HouseholdSetupDestination.Select),
        ) {
            subclass(HouseholdSetupDestination.Select::class)
            subclass(HouseholdSetupDestination.Create::class)
            subclass(HouseholdSetupDestination.Join::class)
        }
    val navigator = remember(state) { Navigator(state = state) }

    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { destination ->
        when (destination) {
            HouseholdSetupDestination.Select ->
                Scaffold(
                    topBar = {
                        Toolbar(
                            text =
                                stringResource(
                                    Res.string.portal_households,
                                ),
                            showBackButton = onBack != null,
                            onBack = { onBack?.invoke() },
                            actions = {
                                if (onAccount != null)
                                    TextButton(onClick = onAccount) {
                                        Text(
                                            stringResource(
                                                Res.string.portal_my_account,
                                            ),
                                        )
                                    }
                            },
                        )
                    },
                ) { padding ->
                    Box(
                        Modifier.padding(padding).consumeWindowInsets(padding),
                    ) {
                        HouseholdScreen(
                            viewModel = selectionViewModel,
                            onSelect = onSelect,
                            onCreate = { navigator.navigateTo(HouseholdSetupDestination.Create) },
                            onJoin = { navigator.navigateTo(HouseholdSetupDestination.Join) },
                        )
                    }
                }
            HouseholdSetupDestination.Create ->
                CreateHouseholdScreen(
                    viewModel = koinViewModel(),
                    onCreated = onSelect,
                    onBack = navigator::goBack,
                )
            HouseholdSetupDestination.Join ->
                JoinHouseholdScreen(
                    viewModel = koinViewModel(),
                    onJoined = {
                        selectionViewModel.onRefresh()
                        navigator.goBack()
                    },
                    onBack = navigator::goBack,
                )
        }
    }
}
