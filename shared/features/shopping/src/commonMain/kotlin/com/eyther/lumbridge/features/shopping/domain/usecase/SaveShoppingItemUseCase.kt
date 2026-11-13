package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

class SaveShoppingItemUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(item: ShoppingItemDomain): AppResult<ShoppingItemDomain> =
        safeCall {
            require(item.version > 0) { "shopping.message_refresh_the_item_before_editing_it" }
            source.save(item.validated()).getOrThrow()
        }
}
