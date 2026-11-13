package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain

class SaveShoppingListUseCase(
    private val shoppingDataSource: ShoppingDataSource,
) {

    suspend operator fun invoke(list: ShoppingListDomain): AppResult<ShoppingListDomain> =
        safeCall {
            shoppingDataSource.saveList(list.validated()).getOrThrow()
        }
}
