package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class HomeScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<HomeState>

    abstract fun onRetry()

    abstract fun onArticleOpened(id: String)

    abstract fun onTasksErrorDismissed()

    abstract fun onShoppingErrorDismissed()

    abstract fun onFeedErrorDismissed()
}
