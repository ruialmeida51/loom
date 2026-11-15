package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoShoppingSelectionDataSource(private val store: DemoHouseholdStore) :
    ShoppingSelectionDataSource {
    override fun stream(): StateFlow<String?> = store.selectedShoppingList.asStateFlow()

    override suspend fun selectList(id: String): AppResult<Unit> {
        store.selectedShoppingList.value = id
        return AppResult.Success(Unit)
    }
}
