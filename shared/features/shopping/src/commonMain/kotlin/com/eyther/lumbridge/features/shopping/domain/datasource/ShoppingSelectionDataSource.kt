package com.eyther.lumbridge.features.shopping.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import kotlinx.coroutines.flow.StateFlow

interface ShoppingSelectionDataSource {
    fun stream(): StateFlow<String?>

    suspend fun selectList(id: String): AppResult<Unit>
}
