package com.eyther.lumbridge.app.home.data

import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class EmptyHomeViewModel : HomeScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(
            HomeState(
                overview =
                    HomeOverviewDomain(
                        shopping = emptyList(),
                        reminders = emptyList(),
                        balanceMinor = 0L,
                        monthSpendingMinor = 0L,
                    ),
            ),
        )
    override val state = mutableState.asStateFlow()

    override fun onDismissError() = Unit
}
