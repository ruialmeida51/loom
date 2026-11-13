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
        val saved = items.map { it.copy(version = 1) }
        store.shopping.update {
            it.copy(
                items =
                    it.items.filterNot { row -> row.id in saved.map { item -> item.id } } + saved,
            )
        }
        saved
    }

    override suspend fun saveItem(item: ShoppingItemDomain) = safeCall {
        val before = store.shopping.value.items.firstOrNull { it.id == item.id } ?: throw ShoppingError.ItemNotFound()
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
}
