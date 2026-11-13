package com.eyther.lumbridge.features.feed.presentation.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.ObserveFeedUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedState
import kotlinx.coroutines.flow.*

class FeedScreenViewModel(
    observe: ObserveFeedUseCase,
    private val followFeed: FollowFeedUseCase,
    private val mark: MarkArticleReadUseCase,
) : FeedScreenViewModelContract() {
    private data class InnerInputState(val url: String = "")

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(FeedState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) { copy(url = it.url) }
        }
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            observe()
                .catch { error -> mutableState.update { it.copy(error = error.message) } }
                .onEach { items -> mutableState.update { it.copy(items = items) } }
        }
    }

    override fun url(value: String) {
        inputState.update { it.copy(url = value) }
    }

    override fun showFollow(value: Boolean) {
        if (value) inputState.value = InnerInputState()
        mutableState.update { it.copy(showFollow = value) }
    }

    override fun unreadOnly(value: Boolean) {
        mutableState.update { it.copy(unreadOnly = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun follow() {
        if (mutableState.value.busy) return
        val url = inputState.value.url
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = followFeed(url)) {
                is AppResult.Success ->
                    mutableState.update { it.copy(showFollow = false, url = "", busy = false) }
                is AppResult.Failure ->
                    mutableState.update { it.copy(error = result.message, busy = false) }
            }
        }
    }

    override fun markRead(item: ArticleDomain, read: Boolean) {
        mutableState.update { old ->
            old.copy(items = old.items.map { if (it.id == item.id) it.copy(read = read) else it })
        }
        launch {
            val result = mark(item.id, read)
            if (result is AppResult.Failure) mutableState.update { it.copy(error = result.message) }
        }
    }
}
