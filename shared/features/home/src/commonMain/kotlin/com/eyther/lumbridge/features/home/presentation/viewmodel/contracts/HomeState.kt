package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.mapper.toPreviewUi
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HomeState(
    val today: String,
    val timeZone: String,
    val taskItems: List<TaskDomain>? = null,
    val shoppingPreview: List<ShoppingItemDomain>? = null,
    val feedItems: List<ArticleDomain>? = null,
    val tasksLoading: Boolean = true,
    val shoppingLoading: Boolean = true,
    val feedLoading: Boolean = true,
    val tasksRefreshing: Boolean = false,
    val shoppingRefreshing: Boolean = false,
    val feedRefreshing: Boolean = false,
    val tasksError: TextResource? = null,
    val shoppingError: TextResource? = null,
    val feedError: TextResource? = null,
) {
    val refreshing: Boolean = tasksRefreshing || shoppingRefreshing || feedRefreshing
    val tasks: ImmutableList<HomeTaskUi> =
        taskItems
            .orEmpty()
            .take(3)
            .map { it.toHomeUi(today = today, timeZone = timeZone) }
            .toImmutableList()
    val shoppingItems: ImmutableList<ShoppingItemDomain> =
        shoppingPreview.orEmpty().take(HOME_SHOPPING_CHIPS).toImmutableList()
    val moreShoppingCount: Int = (shoppingPreview.orEmpty().size - shoppingItems.size).coerceAtLeast(0)
    val articles: ImmutableList<ArticlePreviewUi> =
        feedItems.orEmpty().take(3).map { it.toPreviewUi(timeZone = timeZone) }.toImmutableList()
}

/** Enough chips to recognise the list at a glance; the rest collapse into “+N more”. */
private const val HOME_SHOPPING_CHIPS = 5
