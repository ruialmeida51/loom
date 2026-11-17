package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import kotlinx.coroutines.flow.StateFlow

abstract class FeedSubscriptionsScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<FeedSubscriptionsState>

    abstract fun onRefresh()

    abstract fun onAddFeed()

    abstract fun onEditFeed(subscription: FeedSubscriptionDomain)

    abstract fun onRefreshFeed(subscription: FeedSubscriptionDomain)

    abstract fun onRemoveFeed(subscription: FeedSubscriptionDomain)

    abstract fun onConfirmRemoval()

    abstract fun onDismissRemoval()

    abstract fun onUrlChanged(value: String)

    abstract fun onSaveFeed()

    abstract fun onCloseForm()

    abstract fun onDismissError()

    abstract fun onFeedbackDismissed()
}
