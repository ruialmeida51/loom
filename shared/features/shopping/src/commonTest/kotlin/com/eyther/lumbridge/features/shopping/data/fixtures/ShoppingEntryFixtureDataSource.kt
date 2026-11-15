package com.eyther.lumbridge.features.shopping.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ShoppingEntryFixtureDataSource : ShoppingDataSource {
    val requests = mutableListOf<List<ShoppingItemDomain>>()
    val responses = Channel<AppResult<List<ShoppingItemDomain>>>(Channel.UNLIMITED)
    private val snapshot =
        MutableStateFlow<AppResult<ShoppingSnapshotDomain>>(
            AppResult.Success(
                ShoppingSnapshotDomain(
                    lists =
                        listOf(
                            ShoppingListDomain(id = "groceries", name = "Groceries"),
                            ShoppingListDomain(id = "holiday", name = "Holiday"),
                        ),
                ),
            ),
        )

    override fun stream(): Flow<AppResult<ShoppingSnapshotDomain>> = snapshot

    override suspend fun addItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> {
        requests += items
        return responses.receive()
    }

    override suspend fun saveItem(item: ShoppingItemDomain): AppResult<ShoppingItemDomain> =
        AppResult.Success(item)

    override suspend fun saveList(list: ShoppingListDomain): AppResult<ShoppingListDomain> =
        AppResult.Success(list)
}
