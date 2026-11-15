package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import kotlinx.coroutines.flow.StateFlow

class GetShoppingSelectionStreamUseCase(
    private val shoppingSelectionDataSource: ShoppingSelectionDataSource,
) {
    operator fun invoke(): StateFlow<String?> = shoppingSelectionDataSource.stream()
}
