package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

class SaveShoppingItemUseCase(
    private val shoppingDataSource: ShoppingDataSource,
) {

    suspend operator fun invoke(item: ShoppingItemDomain): AppResult<ShoppingItemDomain> =
        safeCall {
            requireDomain(item.version > 0) { ShoppingError.VersionRequired() }
            shoppingDataSource.saveItem(item.validated()).getOrThrow()
        }

    suspend fun saveGroup(items: List<ShoppingItemDomain>): AppResult<List<ShoppingItemDomain>> =
        safeCall {
            requireDomain(items.isNotEmpty() && items.map { it.id }.distinct().size == items.size) {
                ShoppingError.InvalidBatchSize()
            }
            items.forEach { requireDomain(it.version > 0) { ShoppingError.VersionRequired() } }
            shoppingDataSource.saveGroup(items.map { it.validated() }).getOrThrow()
        }
}
