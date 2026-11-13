package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

class AddShoppingItemUseCase(private val source: ShoppingDataSource) {
    suspend operator fun invoke(item: ShoppingItemDomain): AppResult<Unit> {
        if (item.name.trim().length !in 1..120)
            return AppResult.Failure("Give the item a name, up to 120 characters.")
        if (item.quantity.length > 40)
            return AppResult.Failure("Keep the quantity under 40 characters.")
        return source.add(item.copy(name = item.name.trim(), quantity = item.quantity.trim()))
    }
}
