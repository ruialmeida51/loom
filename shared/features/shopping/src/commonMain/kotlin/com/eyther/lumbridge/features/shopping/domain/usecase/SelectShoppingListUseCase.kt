package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError

class SelectShoppingListUseCase(
    private val shoppingSelectionDataSource: ShoppingSelectionDataSource,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> =
        if (id.isBlank()) AppResult.Failure(ShoppingError.InvalidListOrCategory())
        else shoppingSelectionDataSource.selectList(id = id)
}
