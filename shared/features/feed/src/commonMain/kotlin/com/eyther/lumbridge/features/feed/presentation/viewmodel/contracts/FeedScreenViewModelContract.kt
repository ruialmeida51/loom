package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.StateFlow

abstract class FeedScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<FeedState>

    abstract fun onUrlChanged(value: String)

    abstract fun onFollowSheetVisibilityChanged(value: Boolean)

    abstract fun onUnreadOnlyChanged(value: Boolean)

    abstract fun onDismissError()

    abstract fun onFollowFeed()

    abstract fun onMarkRead(item: ArticleDomain, read: Boolean)
}
