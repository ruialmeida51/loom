package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.navigation.model.HouseholdSetupDestination
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.presentation.screen.CreateHouseholdScreen
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdScreen
import com.eyther.lumbridge.features.household.presentation.screen.JoinHouseholdScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HouseholdSetupHost(
    userId: String,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
) {
    val selectionViewModel: HouseholdScreenViewModelContract = koinViewModel(key = "households-$userId")
    val state = rememberNavigationState(
        startRoot = HouseholdSetupDestination.Select,
        roots = setOf(HouseholdSetupDestination.Select),
    ) {
        subclass(HouseholdSetupDestination.Select::class)
        subclass(HouseholdSetupDestination.Create::class)
        subclass(HouseholdSetupDestination.Join::class)
    }
    val navigator = remember(state) { Navigator(state = state) }
    LaunchedEffect(selectionViewModel) { selectionViewModel.onRefresh() }

    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { destination ->
        when (destination) {
            HouseholdSetupDestination.Select -> HouseholdScreen(
                viewModel = selectionViewModel,
                onSelect = onSelect,
                onSignOut = onSignOut,
                onCreate = { navigator.navigateTo(HouseholdSetupDestination.Create) },
                onJoin = { navigator.navigateTo(HouseholdSetupDestination.Join) },
            )
            HouseholdSetupDestination.Create -> CreateHouseholdScreen(
                viewModel = koinViewModel(),
                onCreated = onSelect,
                onBack = navigator::goBack,
            )
            HouseholdSetupDestination.Join -> JoinHouseholdScreen(
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
