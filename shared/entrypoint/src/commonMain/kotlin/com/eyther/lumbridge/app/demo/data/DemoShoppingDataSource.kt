package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoShoppingDataSource(private val store: DemoHouseholdStore) : ShoppingDataSource {

    override fun stream() = store.shopping.map { AppResult.Success(it) }

    override suspend fun addItems(items: List<ShoppingItemDomain>) = safeCall {
        items.forEach { item ->
            requireDomain(store.shopping.value.activeLists.any { it.id == item.listId }) {
                ShoppingError.ActiveListRequired()
            }
            item.validated()
        }
        val nextPosition = (store.shopping.value.items.maxOfOrNull { it.position } ?: 0L) + 1
        val saved = items.mapIndexed { index, item ->
            item.copy(version = 1, position = nextPosition + index)
        }
        store.shopping.update {
            it.copy(
                items =
                    it.items.filterNot { row -> row.id in saved.map { item -> item.id } } + saved,
            )
        }
        saved
    }

    override suspend fun saveItem(item: ShoppingItemDomain) = safeCall {
        val before =
            store.shopping.value.items.firstOrNull { it.id == item.id }
                ?: throw ShoppingError.ItemNotFound()
        requireDomain(before.version == item.version) {
            ShoppingError.ItemChanged()
        }
        requireDomain(store.shopping.value.activeLists.any { it.id == item.listId }) {
            ShoppingError.ActiveListRequired()
        }
        val saved = item.validated().copy(version = item.version + 1)
        store.shopping.update {
            it.copy(items = it.items.map { old -> if (old.id == item.id) saved else old })
        }
        saved
    }

    override suspend fun saveGroup(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        val existing = store.shopping.value.items.associateBy { it.id }
        items.forEach { item ->
            requireDomain(existing[item.id]?.version == item.version) {
                ShoppingError.ItemChanged()
            }
            item.validated()
        }
        val saved = items.map { item ->
            if (existing[item.id] == item) item else item.copy(version = item.version + 1)
        }
        val byId = saved.associateBy { it.id }
        store.shopping.update { oldState ->
            oldState.copy(items = oldState.items.map { byId[it.id] ?: it })
        }
        saved
    }

    override suspend fun saveList(list: ShoppingListDomain) = safeCall {
        val before = store.shopping.value.lists.firstOrNull { it.id == list.id }
        requireDomain(before == null || before.version == list.version) {
            ShoppingError.ListChanged()
        }
        requireDomain(
            !list.archived || before?.archived == true || store.shopping.value.activeLists.size > 1,
        ) {
            ShoppingError.LastActiveList()
        }
        requireDomain(
            list.archived ||
                store.shopping.value.activeLists.none {
                    it.id != list.id && it.name.equals(list.name.trim(), true)
                },
        ) {
            ShoppingError.ListNameAlreadyExists()
        }
        val saved = list.validated().copy(version = list.version + 1)
        store.shopping.update { it.copy(lists = it.lists.filterNot { it.id == list.id } + saved) }
        saved
    }

    override suspend fun removeCompletedItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> = safeCall {
        items.forEach { target ->
            val current = store.shopping.value.items.firstOrNull { it.id == target.id }
            requireDomain(
                current?.version == target.version && current.checked && !current.removed,
            ) {
                ShoppingError.ItemChanged()
            }
        }
        val before = store.shopping.value
        var snapshot = before
        items.forEach { target ->
            val item = snapshot.items.first { it.id == target.id }
            val changes = snapshot.changeGroup(item.copy(removed = true)).associateBy { it.id }
            snapshot = snapshot.copy(items = snapshot.items.map { changes[it.id] ?: it })
        }
        val originals = before.items.associateBy { it.id }
        val saved =
            snapshot.items.map { item ->
                if (originals[item.id] == item) item else item.copy(version = item.version + 1)
            }
        store.shopping.value = snapshot.copy(items = saved)
        saved.filter { it.listId == items.first().listId }
    }

    override suspend fun deleteList(list: ShoppingListDomain): AppResult<Unit> = safeCall {
        val existing = store.shopping.value.lists.firstOrNull { it.id == list.id }
        requireDomain(existing == null || existing.version == list.version) {
            ShoppingError.ListChanged()
        }
        store.shopping.update { snapshot ->
            snapshot.copy(
                lists = snapshot.lists.filterNot { it.id == list.id },
                items = snapshot.items.filterNot { it.listId == list.id },
            )
        }
    }
}
