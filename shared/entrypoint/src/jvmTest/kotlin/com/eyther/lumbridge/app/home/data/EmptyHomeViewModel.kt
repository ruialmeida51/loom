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
                loading = false,
                overview =
                    HomeOverviewDomain(
                        reminders = emptyList(),
                    ),
            ),
        )
    override val state = mutableState.asStateFlow()

    override fun onRetry() = Unit

    override fun onDismissError() = Unit
}
