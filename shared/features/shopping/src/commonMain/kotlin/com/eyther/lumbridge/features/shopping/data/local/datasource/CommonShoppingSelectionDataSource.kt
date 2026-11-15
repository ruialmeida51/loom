package com.eyther.lumbridge.features.shopping.data.local.datasource

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommonShoppingSelectionDataSource(context: HouseholdContextDomain) :
    ShoppingSelectionDataSource {
    private val key = "shopping-list-${context.userId}-${context.id}"
    private val selectedId = MutableStateFlow(PlatformStorage.get(key))

    override fun stream(): StateFlow<String?> = selectedId.asStateFlow()

    override suspend fun selectList(id: String): AppResult<Unit> = safeApiCall {
        PlatformStorage.put(key, id)
        selectedId.value = id
    }
}
