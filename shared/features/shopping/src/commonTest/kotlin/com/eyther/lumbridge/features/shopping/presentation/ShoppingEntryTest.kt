package com.eyther.lumbridge.features.shopping.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingAnalyticsFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingEntryFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingSelectionFixtureDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.DeleteShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SelectShoppingListUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingEntryTest {
    @Test
    fun failedSubmissionKeepsItsRowAndRetryKeyWhileTheNextDraftStaysAvailable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            viewModel.onInlineNameChanged("Milk")
            viewModel.onAddInlineItem()
            runCurrent()
            assertTrue(viewModel.state.value.addingInline)
            source.responses.send(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertEquals("", viewModel.state.value.inlineName)
            val failed = viewModel.state.value.pendingEntries.single()
            assertEquals("Milk", failed.item.name)
            assertNotNull(failed.error)
            viewModel.onInlineNameChanged("Bread draft")
            runCurrent()
            viewModel.onRetryEntry(id = failed.item.id)
            runCurrent()
            assertEquals(source.requests[0].single().id, source.requests[1].single().id)
            source.responses.send(
                AppResult.Success(source.requests.last().map { it.copy(version = 1) }),
            )
            runCurrent()
            assertEquals("Bread draft", viewModel.state.value.inlineName)
            assertFalse(viewModel.state.value.addingInline)
            assertEquals(1, viewModel.state.value.snapshot.items.size)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun theNextTypedItemSurvivesAnInFlightSaveAndGetsANewRequestId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            viewModel.onInlineNameChanged("Milk")
            viewModel.onAddInlineItem()
            runCurrent()
            viewModel.onInlineNameChanged("Bread")
            viewModel.onAddInlineItem()
            runCurrent()
            assertEquals(1, source.requests.size, "Enter cannot duplicate an in-flight write")
            source.responses.send(
                AppResult.Success(source.requests.first().map { it.copy(version = 1) }),
            )
            runCurrent()
            assertEquals("", viewModel.state.value.inlineName)
            assertEquals(2, source.requests.size, "The second Enter is queued, not dropped")
            assertEquals("Bread", source.requests.last().single().name)
            assertNotEquals(source.requests[0].single().id, source.requests[1].single().id)
            source.responses.send(
                AppResult.Success(source.requests.last().map { it.copy(version = 1) }),
            )
            runCurrent()
            assertEquals("", viewModel.state.value.inlineName)
            assertEquals(2, viewModel.state.value.snapshot.items.size)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun selectingAListSurvivesRecreatingTheHouseholdViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val selection = ShoppingSelectionFixtureDataSource()
        val store = ViewModelStore()
        try {
            val first =
                createViewModel(source = source, selection = selection).also {
                    store.put("first", it)
                }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                first.state.collect()
            }
            runCurrent()
            first.onListSelected("holiday")
            runCurrent()
            val restored =
                createViewModel(source = source, selection = selection).also {
                    store.put("restored", it)
                }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                restored.state.collect()
            }
            runCurrent()
            assertEquals("holiday", restored.state.value.selectedListId)
            restored.onInlineNameChanged("Sun cream")
            restored.onAddInlineItem()
            runCurrent()
            assertEquals("holiday", source.requests.single().single().listId)
            source.responses.send(
                AppResult.Success(source.requests.single().map { it.copy(version = 1) }),
            )
            runCurrent()
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aLateSnapshotCannotEraseAnAcknowledgedItemButANewerServerVersionWins() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val original = (source.snapshot.value as AppResult.Success).value
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            viewModel.onInlineNameChanged("Milk")
            viewModel.onAddInlineItem()
            runCurrent()
            val saved = source.requests.single().single().copy(version = 1, position = 10)
            source.responses.send(AppResult.Success(listOf(saved)))
            runCurrent()
            source.snapshot.value =
                AppResult.Success(
                    original.copy(
                        lists =
                            original.lists.map {
                                it.copy(version = 2)
                            },
                    ),
                )
            runCurrent()
            assertEquals(listOf(saved), viewModel.state.value.uncheckedItems)
            val remote = saved.copy(name = "Oat milk", version = 2)
            source.snapshot.value = AppResult.Success(original.copy(items = listOf(remote)))
            runCurrent()
            assertEquals(listOf(remote), viewModel.state.value.uncheckedItems)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun checkingTwoItemsDoesNotDropTheSecondTapAndFailureRollsBackOnlyItsOwnRow() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource().apply { deferItemWrites = true }
        val original = (source.snapshot.value as AppResult.Success).value
        val first =
            ShoppingItemDomain(
                "first",
                "Milk",
                "",
                "Other",
                false,
                "groceries",
                version = 1,
                position = 1,
            )
        val second = first.copy(id = "second", name = "Bread", position = 2)
        source.snapshot.value = AppResult.Success(original.copy(items = listOf(first, second)))
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            viewModel.onToggleItem(first)
            viewModel.onToggleItem(second)
            runCurrent()
            assertEquals(setOf(first.id, second.id), source.itemWrites.keys)
            assertFalse(viewModel.state.value.busy)
            assertTrue(viewModel.state.value.snapshot.items.all { it.checked })
            source.itemWrites
                .getValue(second.id)
                .complete(AppResult.Success(second.copy(checked = true, version = 2)))
            source.itemWrites
                .getValue(first.id)
                .complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertFalse(viewModel.state.value.snapshot.items.first { it.id == first.id }.checked)
            assertTrue(viewModel.state.value.snapshot.items.first { it.id == second.id }.checked)
            assertTrue(viewModel.state.value.savingItemIds.isEmpty())
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun deletingTheFinalListLeavesAnExplicitCreateListState() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            for (list in viewModel.state.value.activeLists.toList()) {
                viewModel.onDeleteListRequested(list = list)
                viewModel.onConfirmDeleteList()
                runCurrent()
            }
            assertTrue(viewModel.state.value.activeLists.isEmpty())
            assertEquals(null, viewModel.state.value.selectedList)
            assertFalse(viewModel.state.value.showLists)
            assertFalse(viewModel.state.value.canAddInline)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(
        source: ShoppingEntryFixtureDataSource,
        selection: ShoppingSelectionFixtureDataSource = ShoppingSelectionFixtureDataSource(),
    ): ShoppingScreenViewModel =
        ShoppingScreenViewModel(
            getShoppingStreamUseCase = GetShoppingStreamUseCase(shoppingDataSource = source),
            getShoppingSelectionStreamUseCase =
                GetShoppingSelectionStreamUseCase(shoppingSelectionDataSource = selection),
            selectShoppingListUseCase =
                SelectShoppingListUseCase(shoppingSelectionDataSource = selection),
            addShoppingItemsUseCase = AddShoppingItemsUseCase(shoppingDataSource = source),
            saveShoppingItemUseCase = SaveShoppingItemUseCase(shoppingDataSource = source),
            saveShoppingListUseCase = SaveShoppingListUseCase(shoppingDataSource = source),
            deleteShoppingListUseCase = DeleteShoppingListUseCase(shoppingDataSource = source),
            trackAnalyticsEventUseCase =
                TrackAnalyticsEventUseCase(
                    analyticsDataSource = ShoppingAnalyticsFixtureDataSource(),
                ),
        )
}
