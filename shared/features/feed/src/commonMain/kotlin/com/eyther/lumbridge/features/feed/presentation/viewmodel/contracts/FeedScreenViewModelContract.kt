package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import kotlinx.coroutines.flow.StateFlow

abstract class FeedScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<FeedState>

    abstract fun onStarterFeedSelected(feed: StarterFeedDomain)

    abstract fun onRetry()

    abstract fun onSourcesChanged()

    abstract fun onSourceSelected(id: String)

    abstract fun onAllSourcesSelected()

    abstract fun onLoadMore()

    abstract fun onRetryLoadMore()

    abstract fun onUrlChanged(value: String)

    abstract fun onFollowSheetVisibilityChanged(value: Boolean)

    abstract fun onUnreadOnlyChanged(value: Boolean)

    abstract fun onDismissError()

    abstract fun onFollowFeed()

    abstract fun onMarkRead(id: String, read: Boolean)
}
