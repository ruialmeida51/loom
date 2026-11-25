package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.features.home.domain.model.HomeShoppingDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeShoppingStreamUseCase(
    private val getShoppingStreamUseCase: GetShoppingStreamUseCase,
    private val getShoppingSelectionStreamUseCase: GetShoppingSelectionStreamUseCase,
) {
    operator fun invoke(): Flow<AppResult<HomeShoppingDomain>> =
        combine(getShoppingStreamUseCase(), getShoppingSelectionStreamUseCase()) {
            result,
            selectedId ->
            result.mapToResult { snapshot ->
                val listId = snapshot.resolveActiveListId(selectedId.orEmpty())
                HomeShoppingDomain(
                    list = snapshot.activeLists.firstOrNull { it.id == listId },
                    items = snapshot.activeItems.filter { !it.checked && it.listId == listId },
                )
            }
        }
}
