package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

class AddShoppingItemsUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        require(items.size in 1..50) { "shopping.message_add_between_1_and_50_items_at_once" }
        require(items.all { it.version == 0L && !it.removed }) {
            "shopping.message_add_new_items_to_the_list"
        }
        source.add(items.map { it.validated() }).getOrThrow()
    }
}
