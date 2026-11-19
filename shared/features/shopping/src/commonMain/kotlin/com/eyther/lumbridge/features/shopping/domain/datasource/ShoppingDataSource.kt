package com.eyther.lumbridge.features.shopping.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlinx.coroutines.flow.Flow

interface ShoppingDataSource {
    fun stream(): Flow<AppResult<ShoppingSnapshotDomain>>

    suspend fun addItems(items: List<ShoppingItemDomain>): AppResult<List<ShoppingItemDomain>>

    /** Places [item] directly below [afterId]; returns it first, then every row that moved. */
    suspend fun insertItemAfter(
        item: ShoppingItemDomain,
        afterId: String,
    ): AppResult<List<ShoppingItemDomain>>

    suspend fun saveItem(item: ShoppingItemDomain): AppResult<ShoppingItemDomain>

    suspend fun saveGroup(items: List<ShoppingItemDomain>): AppResult<List<ShoppingItemDomain>>

    suspend fun saveList(list: ShoppingListDomain): AppResult<ShoppingListDomain>

    suspend fun removeCompletedItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>>

    suspend fun deleteList(list: ShoppingListDomain): AppResult<Unit>
}
