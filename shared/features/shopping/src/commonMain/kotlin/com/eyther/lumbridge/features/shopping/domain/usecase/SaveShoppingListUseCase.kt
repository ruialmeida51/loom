package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain

class SaveShoppingListUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(list: ShoppingListDomain): AppResult<ShoppingListDomain> =
        safeCall {
            source.saveList(list.validated()).getOrThrow()
        }
}
