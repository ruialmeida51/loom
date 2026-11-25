package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.mapper.toPreviewUi
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import com.eyther.lumbridge.features.home.presentation.mapper.homeBalanceUi
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeBalanceUi
import com.eyther.lumbridge.features.home.presentation.model.HomeGreetingUi
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HomeState(
    val today: String,
    val timeZone: String,
    val userId: String = "",
    val currency: String = "EUR",
    val greeting: HomeGreetingUi = HomeGreetingUi.MORNING,
    val balances: Map<String, Long>? = null,
    val members: List<MemberDomain> = emptyList(),
    val balanceLoading: Boolean = true,
    val balanceRefreshing: Boolean = false,
    val balanceError: TextResource? = null,
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
    val refreshing: Boolean =
        tasksRefreshing || shoppingRefreshing || feedRefreshing || balanceRefreshing

    /** Null until the first load, so the card shows its skeleton rather than a false zero. */
    val balance: HomeBalanceUi? =
        balances?.let {
            homeBalanceUi(
                balances = it,
                userId = userId,
                currency = currency,
                memberNames = members.associate { member -> member.id to member.name },
            )
        }
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
