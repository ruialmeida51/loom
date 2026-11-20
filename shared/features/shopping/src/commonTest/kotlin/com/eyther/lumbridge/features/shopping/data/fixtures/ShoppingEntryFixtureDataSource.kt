package com.eyther.lumbridge.features.shopping.data.fixtures

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ShoppingEntryFixtureDataSource : ShoppingDataSource {
    var removalError: AppError? = null
    val removals = mutableListOf<List<ShoppingItemDomain>>()
    var deferItemWrites: Boolean = false
    val itemWrites = mutableMapOf<String, CompletableDeferred<AppResult<ShoppingItemDomain>>>()
    val requests = mutableListOf<List<ShoppingItemDomain>>()
    val insertAnchors = mutableListOf<String>()
    val responses = Channel<AppResult<List<ShoppingItemDomain>>>(Channel.UNLIMITED)
    val snapshot =
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

    override suspend fun insertItemAfter(
        item: ShoppingItemDomain,
        afterId: String,
    ): AppResult<List<ShoppingItemDomain>> {
        requests += listOf(item)
        insertAnchors += afterId
        return responses.receive()
    }

    override suspend fun saveItem(item: ShoppingItemDomain): AppResult<ShoppingItemDomain> =
        if (deferItemWrites) {
            CompletableDeferred<AppResult<ShoppingItemDomain>>()
                .also { itemWrites[item.id] = it }
                .await()
        } else AppResult.Success(item)

    override suspend fun saveGroup(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        items.map { saveItem(it).getOrThrow() }
    }

    override suspend fun saveList(list: ShoppingListDomain): AppResult<ShoppingListDomain> =
        AppResult.Success(list)

    override suspend fun removeCompletedItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> {
        removals += items
        return removalError?.let { AppResult.Failure(it) }
            ?: AppResult.Success(items.map { it.copy(removed = true, version = it.version + 1) })
    }

    override suspend fun deleteList(list: ShoppingListDomain): AppResult<Unit> {
        val current = (snapshot.value as AppResult.Success).value
        snapshot.value =
            AppResult.Success(
                current.copy(
                    lists = current.lists.filterNot { it.id == list.id },
                    items = current.items.filterNot { it.listId == list.id },
                ),
            )
        return AppResult.Success(Unit)
    }
}
