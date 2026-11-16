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
                reminderItems = emptyList(),
                shoppingPreview = emptyList(),
                feedItems = emptyList(),
                remindersLoading = false,
                shoppingLoading = false,
                feedLoading = false,
            ),
        )
    override val state = mutableState.asStateFlow()

    override fun onRetry() = Unit

    override fun onArticleOpened(id: String) = Unit

    override fun onRemindersErrorDismissed() = Unit

    override fun onShoppingErrorDismissed() = Unit

    override fun onFeedErrorDismissed() = Unit
}
