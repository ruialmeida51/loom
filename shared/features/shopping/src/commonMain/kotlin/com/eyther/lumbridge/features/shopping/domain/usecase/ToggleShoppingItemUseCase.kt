package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource

class ToggleShoppingItemUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(id: String, checked: Boolean) = source.toggle(id, checked)
}
