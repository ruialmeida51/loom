package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.*

data class FeedState(
    val items: List<ArticleDomain> = emptyList(),
    val url: String = "",
    val showFollow: Boolean = false,
    val unreadOnly: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class FeedScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<FeedState>

    abstract fun url(value: String)

    abstract fun showFollow(value: Boolean)

    abstract fun unreadOnly(value: Boolean)

    abstract fun dismiss()

    abstract fun follow()

    abstract fun markRead(item: ArticleDomain, read: Boolean)
}
