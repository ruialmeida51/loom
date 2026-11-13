package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource

class ObserveShoppingUseCase(private val source: ShoppingDataSource) {
    operator fun invoke() = source.observe()
}
