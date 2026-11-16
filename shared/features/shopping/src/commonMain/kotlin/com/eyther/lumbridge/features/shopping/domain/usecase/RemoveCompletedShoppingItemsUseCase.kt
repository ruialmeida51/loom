package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

/** One transactional removal; a restored or edited item causes the whole request to fail. */
class RemoveCompletedShoppingItemsUseCase(private val shoppingDataSource: ShoppingDataSource) {
    suspend operator fun invoke(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        requireDomain(items.size in 1..MAX_BATCH_SIZE) { ShoppingError.InvalidCompletedSelection() }
        requireDomain(
            items.all { it.checked && !it.removed && it.version > 0 } &&
                items.map { it.id }.distinct().size == items.size &&
                items.map { it.listId }.distinct().size == 1,
        ) {
            ShoppingError.InvalidCompletedSelection()
        }
        shoppingDataSource.removeCompletedItems(items = items).getOrThrow()
    }

    companion object {
        const val MAX_BATCH_SIZE = 1000
    }
}
