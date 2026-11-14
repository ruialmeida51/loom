package com.eyther.lumbridge.features.shopping.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlinx.coroutines.flow.Flow

class GetShoppingStreamUseCase(
    private val shoppingDataSource: ShoppingDataSource,
) {

    operator fun invoke(): Flow<AppResult<ShoppingSnapshotDomain>> = shoppingDataSource.stream()
}
