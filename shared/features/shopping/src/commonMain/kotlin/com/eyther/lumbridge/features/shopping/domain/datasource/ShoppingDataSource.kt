package com.eyther.lumbridge.features.shopping.domain.datasource

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.shopping.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ShoppingDataSource {
    fun observe(): Flow<ShoppingSnapshotDomain>

    suspend fun add(items: List<ShoppingItemDomain>): AppResult<List<ShoppingItemDomain>>

    suspend fun save(item: ShoppingItemDomain): AppResult<ShoppingItemDomain>

    suspend fun saveList(list: ShoppingListDomain): AppResult<ShoppingListDomain>
}
