package com.eyther.lumbridge.features.home.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class HomeShoppingPreviewTest {
    @Test
    fun previewUsesTheSameSelectedListAsShoppingAndExcludesCompletedItems() = runTest {
        val selection = MutableStateFlow<String?>("trip")
        val snapshot =
            ShoppingSnapshotDomain(
                lists =
                    listOf(
                        ShoppingListDomain("groceries", "Groceries"),
                        ShoppingListDomain("trip", "Trip"),
                    ),
                items =
                    listOf(
                        item("milk", "groceries", false),
                        item("coffee", "trip", false),
                        item("tea", "trip", true),
                    ),
            )
        val useCase = overview(selection = selection, shopping = AppResult.Success(snapshot))
        assertEquals(listOf("coffee"), useCase().first().getOrThrow().map { it.name })
        selection.value = "groceries"
        assertEquals(listOf("milk"), useCase().first().getOrThrow().map { it.name })
    }

    @Test
    fun aShoppingFailureRemainsTypedInsteadOfBecomingAnEmptyList() = runTest {
        val result =
            overview(MutableStateFlow(null), AppResult.Failure(CommonError.NoConnection()))()
                .first()
        assertIs<CommonError.NoConnection>(assertIs<AppResult.Failure>(result).error)
    }

    private fun item(name: String, list: String, checked: Boolean): ShoppingItemDomain =
        ShoppingItemDomain(
            id = name,
            name = name,
            quantity = "",
            category = "Other",
            checked = checked,
            listId = list,
        )

    private fun overview(
        selection: MutableStateFlow<String?>,
        shopping: AppResult<ShoppingSnapshotDomain>,
    ): GetHomeShoppingStreamUseCase {
        val shoppingSource =
            object : ShoppingDataSource {
                override fun stream() = flowOf(shopping)

                override suspend fun addItems(
                    items: List<ShoppingItemDomain>,
                ): AppResult<List<ShoppingItemDomain>> = error("Read-only test")

                override suspend fun insertItemAfter(
                    item: ShoppingItemDomain,
                    afterId: String,
                ): AppResult<List<ShoppingItemDomain>> = error("Read-only test")

                override suspend fun saveItem(
                    item: ShoppingItemDomain,
                ): AppResult<ShoppingItemDomain> = error("Read-only test")

                override suspend fun saveGroup(
                    items: List<ShoppingItemDomain>,
                ): AppResult<List<ShoppingItemDomain>> = safeCall {
                    items.map { saveItem(it).getOrThrow() }
                }

                override suspend fun saveList(
                    list: ShoppingListDomain,
                ): AppResult<ShoppingListDomain> = error("Read-only test")

                override suspend fun deleteList(list: ShoppingListDomain): AppResult<Unit> =
                    error("Read-only test")

                override suspend fun removeCompletedItems(
                    items: List<ShoppingItemDomain>,
                ): AppResult<List<ShoppingItemDomain>> = error("Read-only test")
            }
        val selectionSource =
            object : ShoppingSelectionDataSource {
                override fun stream() = selection

                override suspend fun selectList(id: String): AppResult<Unit> =
                    error("Read-only test")
            }
        return GetHomeShoppingStreamUseCase(
            getShoppingStreamUseCase =
                GetShoppingStreamUseCase(shoppingDataSource = shoppingSource),
            getShoppingSelectionStreamUseCase =
                GetShoppingSelectionStreamUseCase(shoppingSelectionDataSource = selectionSource),
        )
    }
}
