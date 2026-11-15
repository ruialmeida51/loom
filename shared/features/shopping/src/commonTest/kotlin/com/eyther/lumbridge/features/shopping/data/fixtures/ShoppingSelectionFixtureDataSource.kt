package com.eyther.lumbridge.features.shopping.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShoppingSelectionFixtureDataSource : ShoppingSelectionDataSource {
    private val selection = MutableStateFlow<String?>(null)

    override fun stream(): StateFlow<String?> = selection

    override suspend fun selectList(id: String): AppResult<Unit> {
        selection.value = id
        return AppResult.Success(Unit)
    }
}
