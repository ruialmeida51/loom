package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain

/**
 * Load, action and sheet errors have separate lifetimes; refreshed articles never erase a form
 * failure.
 */
data class FeedState(
    val items: List<ArticleDomain> = emptyList(),
    val url: String = "",
    val showFollow: Boolean = false,
    val unreadOnly: Boolean = false,
    val busy: Boolean = false,
    val loading: Boolean = true,
    val hasLoaded: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError

    val visibleArticles: List<ArticleDomain> = items.filter { !unreadOnly || !it.read }

    val canFollow: Boolean = url.isNotBlank()
}
