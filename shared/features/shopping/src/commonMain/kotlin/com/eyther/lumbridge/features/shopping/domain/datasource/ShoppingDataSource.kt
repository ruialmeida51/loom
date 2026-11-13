package com.eyther.lumbridge.features.shopping.domain.datasource

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import kotlinx.coroutines.flow.Flow

interface ShoppingDataSource {
    fun observe(): Flow<List<ShoppingItemDomain>>

    suspend fun add(item: ShoppingItemDomain): AppResult<Unit>

    suspend fun toggle(id: String, checked: Boolean): AppResult<Unit>

    suspend fun remove(id: String): AppResult<Unit>
}
