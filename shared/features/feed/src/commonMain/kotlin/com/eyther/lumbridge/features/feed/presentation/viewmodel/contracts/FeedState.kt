package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain

data class FeedState(
    val items: List<ArticleDomain> = emptyList(),
    val url: String = "",
    val showFollow: Boolean = false,
    val unreadOnly: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
)
