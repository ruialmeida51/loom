package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource

class RemoveShoppingItemUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(id: String) = source.remove(id)
}
