package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.*
import kotlinx.coroutines.flow.update

class DemoShoppingDataSource(private val store: DemoHouseholdStore) : ShoppingDataSource {
    override fun observe() = store.shopping

    override suspend fun add(items: List<ShoppingItemDomain>) = safeCall {
        items.forEach { item ->
            require(store.shopping.value.activeLists.any { it.id == item.listId })
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

    override suspend fun save(item: ShoppingItemDomain) = safeCall {
        val before = store.shopping.value.items.first { it.id == item.id }
        require(before.version == item.version) {
            "shopping.message_this_item_changed_refresh_before_undoing_or_editing_it"
        }
        require(store.shopping.value.activeLists.any { it.id == item.listId })
        val saved = item.validated().copy(version = item.version + 1)
        store.shopping.update {
            it.copy(items = it.items.map { old -> if (old.id == item.id) saved else old })
        }
        saved
    }

    override suspend fun saveList(list: ShoppingListDomain) = safeCall {
        val before = store.shopping.value.lists.firstOrNull { it.id == list.id }
        require(before == null || before.version == list.version)
        require(
            !list.archived || before?.archived == true || store.shopping.value.activeLists.size > 1,
        ) {
            "shopping.message_keep_at_least_one_shopping_list_active"
        }
        require(
            list.archived ||
                store.shopping.value.activeLists.none {
                    it.id != list.id && it.name.equals(list.name.trim(), true)
                },
        ) {
            "shopping.message_list_name_exists"
        }
        val saved = list.validated().copy(version = list.version + 1)
        store.shopping.update { it.copy(lists = it.lists.filterNot { it.id == list.id } + saved) }
        saved
    }
}
