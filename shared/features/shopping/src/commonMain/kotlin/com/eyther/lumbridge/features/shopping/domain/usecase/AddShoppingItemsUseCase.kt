package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

class AddShoppingItemsUseCase(
    private val shoppingDataSource: ShoppingDataSource,
) {

    suspend operator fun invoke(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        requireDomain(items.size in 1..50) { ShoppingError.InvalidBatchSize() }
        requireDomain(items.all { it.version == 0L && !it.removed }) {
            ShoppingError.ItemsAlreadySaved()
        }
        shoppingDataSource.addItems(items.map { it.validated() }).getOrThrow()
    }

    /** Keep-style entry: the server places [item] directly below [afterId], or appends. */
    suspend fun insertAfter(
        item: ShoppingItemDomain,
        afterId: String,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        requireDomain(item.version == 0L && !item.removed) { ShoppingError.ItemsAlreadySaved() }
        shoppingDataSource.insertItemAfter(item = item.validated(), afterId = afterId).getOrThrow()
    }
}
