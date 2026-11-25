package com.eyther.lumbridge.app.home.data

import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class EmptyHomeViewModel : HomeScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(
            HomeState(
                today = "2026-11-16",
                timeZone = "UTC",
                taskItems = emptyList(),
                shoppingPreview = emptyList(),
                feedItems = emptyList(),
                tasksLoading = false,
                shoppingLoading = false,
                feedLoading = false,
                balances = emptyMap(),
                balanceLoading = false,
            ),
        )
    override val state = mutableState.asStateFlow()

    override fun onRetry() = Unit

    override fun onArticleReadChanged(id: String, read: Boolean) = Unit

    override fun onTasksErrorDismissed() = Unit

    override fun onShoppingErrorDismissed() = Unit

    override fun onFeedErrorDismissed() = Unit

    override fun onBalanceErrorDismissed() = Unit
}
