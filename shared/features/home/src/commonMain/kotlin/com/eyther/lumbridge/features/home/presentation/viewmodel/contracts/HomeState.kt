package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.mapper.toPreviewUi
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HomeState(
    val today: String,
    val timeZone: String,
    val reminderItems: List<ReminderDomain>? = null,
    val shoppingPreview: List<ShoppingItemDomain>? = null,
    val feedItems: List<ArticleDomain>? = null,
    val remindersLoading: Boolean = true,
    val shoppingLoading: Boolean = true,
    val feedLoading: Boolean = true,
    val remindersRefreshing: Boolean = false,
    val shoppingRefreshing: Boolean = false,
    val feedRefreshing: Boolean = false,
    val remindersError: TextResource? = null,
    val shoppingError: TextResource? = null,
    val feedError: TextResource? = null,
) {
    val refreshing: Boolean = remindersRefreshing || shoppingRefreshing || feedRefreshing
    val reminders: ImmutableList<HomeReminderUi> =
        reminderItems
            .orEmpty()
            .take(3)
            .map { it.toHomeUi(today = today, timeZone = timeZone) }
            .toImmutableList()
    val shoppingItems: ImmutableList<ShoppingItemDomain> =
        shoppingPreview.orEmpty().take(3).toImmutableList()
    val articles: ImmutableList<ArticlePreviewUi> =
        feedItems.orEmpty().take(3).map { it.toPreviewUi(timeZone = timeZone) }.toImmutableList()
}
