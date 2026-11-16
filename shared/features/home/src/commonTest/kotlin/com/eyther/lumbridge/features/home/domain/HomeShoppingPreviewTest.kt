package com.eyther.lumbridge.features.home.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
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
        assertEquals(listOf("coffee"), useCase().first().getOrThrow().shoppingItems.map { it.name })
        selection.value = "groceries"
        assertEquals(listOf("milk"), useCase().first().getOrThrow().shoppingItems.map { it.name })
    }

    @Test
    fun aShoppingFailureDoesNotHideLoadedReminders() = runTest {
        val result =
            overview(MutableStateFlow(null), AppResult.Failure(CommonError.NoConnection()))()
                .first()
                .getOrThrow()
        assertEquals("Bins", result.reminders.single().title)
        assertIs<CommonError.NoConnection>(result.shoppingError)
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
    ): GetHomeOverviewStreamUseCase {
        val reminderSource =
            object : ReminderDataSource {
                override fun stream() =
                    flowOf(
                        AppResult.Success(
                            listOf(
                                ReminderDomain(
                                    "id",
                                    "Bins",
                                    "2026-11-16T18:00:00Z",
                                    "daily",
                                    false,
                                ),
                            ),
                        ),
                    )

                override suspend fun addReminder(reminder: ReminderDomain): AppResult<Unit> =
                    error("Read-only test")

                override suspend fun completeReminder(reminder: ReminderDomain): AppResult<Unit> =
                    error("Read-only test")

                override suspend fun removeReminder(id: String): AppResult<Unit> =
                    error("Read-only test")
            }
        val shoppingSource =
            object : ShoppingDataSource {
                override fun stream() = flowOf(shopping)

                override suspend fun addItems(
                    items: List<ShoppingItemDomain>,
                ): AppResult<List<ShoppingItemDomain>> = error("Read-only test")

                override suspend fun saveItem(
                    item: ShoppingItemDomain,
                ): AppResult<ShoppingItemDomain> = error("Read-only test")

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
        return GetHomeOverviewStreamUseCase(
            getRemindersStreamUseCase =
                GetRemindersStreamUseCase(reminderDataSource = reminderSource),
            getShoppingStreamUseCase =
                GetShoppingStreamUseCase(shoppingDataSource = shoppingSource),
            getShoppingSelectionStreamUseCase =
                GetShoppingSelectionStreamUseCase(shoppingSelectionDataSource = selectionSource),
            context =
                HouseholdContextDomain(
                    id = "home",
                    userId = "you",
                    name = "Home",
                    currency = "EUR",
                    timeZone = "UTC",
                ),
        )
    }
}
