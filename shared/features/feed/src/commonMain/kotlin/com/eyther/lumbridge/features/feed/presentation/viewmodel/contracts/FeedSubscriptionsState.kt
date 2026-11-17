package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_remove
import com.eyther.lumbridge.features.feed.generated.resources.feed_remove_description
import com.eyther.lumbridge.features.feed.generated.resources.feed_remove_title
import com.eyther.lumbridge.features.feed.generated.resources.ui_cancel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class FeedSubscriptionsState(
    val items: ImmutableList<FeedSubscriptionDomain> = persistentListOf(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val hasLoaded: Boolean = false,
    val busy: Boolean = false,
    val busyId: String? = null,
    val url: String = "",
    val showForm: Boolean = false,
    val editing: Boolean = false,
    val removing: FeedSubscriptionDomain? = null,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
    val feedback: TextResource? = null,
) {
    val displayError: TextResource? = error ?: loadError
    val canSave: Boolean = url.isNotBlank() && !busy
    val deleteConfirmation: DestructiveConfirmationUi? = removing?.let { subscription ->
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.feed_remove_title),
            message =
                TextResource.Resource(
                    Res.string.feed_remove_description,
                    args = listOf(subscription.publisher),
                ),
            confirmText = TextResource.Resource(Res.string.feed_remove),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
}
