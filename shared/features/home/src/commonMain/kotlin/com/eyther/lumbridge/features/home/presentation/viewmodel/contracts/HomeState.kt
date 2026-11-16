package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HomeState(
    val overview: HomeOverviewDomain? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val timeZone: String = "UTC",
    val error: TextResource? = null,
) {
    val hasOverview: Boolean = overview != null
    val reminders: ImmutableList<HomeReminderUi> =
        overview
            ?.reminders
            .orEmpty()
            .take(4)
            .map {
                it.toHomeUi(today = overview?.today.orEmpty(), timeZone = timeZone)
            }
            .toImmutableList()
    val shoppingItems: ImmutableList<ShoppingItemDomain> =
        overview?.shoppingItems.orEmpty().take(3).toImmutableList()
    val shoppingError: TextResource? = overview?.shoppingError?.toShoppingTextResource()
}
