package com.eyther.lumbridge.features.shopping.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ACTION_ALERT_DURATION
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingAnalyticsFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingEntryFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingSelectionFixtureDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.DeleteShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveCompletedShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SelectShoppingListUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
            advanceTimeBy(300)
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
            advanceTimeBy(300)
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
            advanceTimeBy(300)
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
            advanceTimeBy(300)
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
            advanceTimeBy(300)
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
            advanceTimeBy(300)
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

    @Test
    fun refreshKeepsDraftAndSelectionWhenTheNetworkFails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val original = (source.snapshot.value as AppResult.Success).value
        source.snapshot.value =
            AppResult.Success(
                original.copy(
                    items =
                        listOf(
                            ShoppingItemDomain(
                                id = "done",
                                name = "Milk",
                                quantity = "",
                                category = "Other",
                                listId = "groceries",
                                checked = true,
                                version = 1,
                            ),
                        ),
                ),
            )
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onInlineNameChanged("Bread draft")
            viewModel.onCompletedSelectionChanged(true)
            viewModel.onSelectAllCompleted()
            runCurrent()
            viewModel.onRetry()
            assertTrue(viewModel.state.value.refreshing)
            source.snapshot.value = AppResult.Failure(CommonError.NoConnection())
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertFalse(viewModel.state.value.refreshing)
            assertNotNull(viewModel.state.value.loadError)
            assertEquals("Bread draft", viewModel.state.value.inlineName)
            assertEquals(setOf("done"), viewModel.state.value.selectedCompletedIds)
            assertEquals(1, viewModel.state.value.completedItems.size)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun selectingCompletedItemsDoesNotChangeTheirCompletionAndBulkFailureKeepsThem() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val original = (source.snapshot.value as AppResult.Success).value
        source.snapshot.value =
            AppResult.Success(
                original.copy(
                    items =
                        listOf(
                            ShoppingItemDomain(
                                id = "milk",
                                name = "Milk",
                                quantity = "",
                                category = "Other",
                                listId = "groceries",
                                checked = true,
                                version = 1,
                            ),
                            ShoppingItemDomain(
                                id = "bread",
                                name = "Bread",
                                quantity = "",
                                category = "Other",
                                listId = "groceries",
                                checked = true,
                                version = 1,
                            ),
                            ShoppingItemDomain(
                                id = "apples",
                                name = "Apples",
                                quantity = "",
                                category = "Other",
                                listId = "groceries",
                                checked = false,
                                version = 1,
                            ),
                        ),
                ),
            )
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onCompletedSelectionChanged(true)
            viewModel.onCompletedItemSelected("milk")
            assertEquals(2, viewModel.state.value.completedItems.size)
            assertEquals(setOf("milk"), viewModel.state.value.selectedCompletedIds)
            source.removalError = ShoppingError.ItemChanged()
            viewModel.onDeleteCompletedRequested()
            viewModel.onDeleteCompletedConfirmed()
            runCurrent()
            assertEquals(setOf("milk"), viewModel.state.value.selectedCompletedIds)
            assertEquals(2, viewModel.state.value.completedItems.size)
            assertNotNull(viewModel.state.value.error)
            source.removalError = null
            viewModel.onSelectAllCompleted()
            viewModel.onDeleteCompletedRequested()
            viewModel.onDeleteCompletedConfirmed()
            runCurrent()
            assertEquals(setOf("milk", "bread"), source.removals.last().map { it.id }.toSet())
            assertTrue(viewModel.state.value.completedItems.isEmpty())
            assertEquals("Apples", viewModel.state.value.uncheckedItems.single().name)
            assertNotNull(viewModel.state.value.successMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun groceryRemovalRequiresConfirmationAndFailurePreservesTheOriginalVersionForRetry() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val source = ShoppingEntryFixtureDataSource().also { it.deferItemWrites = true }
            val grocery =
                ShoppingItemDomain(
                    id = "milk",
                    name = "Milk",
                    quantity = "",
                    category = "Other",
                    listId = "groceries",
                    checked = false,
                    version = 4,
                )
            val original = (source.snapshot.value as AppResult.Success).value
            source.snapshot.value = AppResult.Success(original.copy(items = listOf(grocery)))
            val store = ViewModelStore()
            val viewModel = createViewModel(source).also { store.put("shopping", it) }
            try {
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.state.collect()
                }
                runCurrent()
                advanceTimeBy(300)
                runCurrent()
                viewModel.onRemoveItemConfirmed()
                runCurrent()
                assertTrue(source.itemWrites.isEmpty())
                viewModel.onRemoveItem(grocery)
                runCurrent()
                assertNotNull(viewModel.state.value.itemDeleteConfirmation)
                assertTrue(source.itemWrites.isEmpty())
                viewModel.onRemoveItemCancelled()
                assertEquals(null, viewModel.state.value.itemDeleteConfirmation)
                assertEquals(grocery, viewModel.state.value.items.single())
                viewModel.onRemoveItem(grocery)
                viewModel.onRemoveItemConfirmed()
                runCurrent()
                val first = source.itemWrites.getValue(grocery.id)
                viewModel.onRemoveItemConfirmed()
                viewModel.onRemoveItemCancelled()
                runCurrent()
                assertTrue(viewModel.state.value.deletingSingleItem)
                assertEquals(first, source.itemWrites.getValue(grocery.id))
                first.complete(AppResult.Failure(CommonError.NoConnection()))
                runCurrent()
                assertNotNull(viewModel.state.value.itemDeleteError)
                assertEquals(grocery, viewModel.state.value.items.single())
                viewModel.onRemoveItemConfirmed()
                runCurrent()
                source.itemWrites
                    .getValue(grocery.id)
                    .complete(AppResult.Success(grocery.copy(removed = true, version = 5)))
                runCurrent()
                assertEquals(null, viewModel.state.value.itemDeleteConfirmation)
                assertTrue(viewModel.state.value.items.isEmpty())
                assertNotNull(viewModel.state.value.undoMessage)
            } finally {
                store.clear()
                Dispatchers.resetMain()
            }
        }

    @Test
    fun removalFeedbackExpiresAndTheSameMessageCanBeShownAgain() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val initial = (source.snapshot.value as AppResult.Success).value
        val milk = feedbackItem(id = "Milk")
        val bread = feedbackItem(id = "Bread")
        source.snapshot.value = AppResult.Success(initial.copy(items = listOf(milk, bread)))
        val store = ViewModelStore()
        val model = createViewModel(source = source).also { store.put("shopping", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            model.onRemoveItem(milk)
            model.onRemoveItemConfirmed()
            runCurrent()
            val message = model.state.value.undoMessage
            assertNotNull(message)
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION)
            runCurrent()
            assertNull(model.state.value.undoMessage)
            model.onUndo()
            runCurrent()
            assertTrue(model.state.value.snapshot.items.first { it.id == milk.id }.removed)
            model.onRemoveItem(bread)
            model.onRemoveItemConfirmed()
            runCurrent()
            assertEquals(message, model.state.value.undoMessage)
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION - 1)
            runCurrent()
            assertNotNull(model.state.value.undoMessage)
            advanceTimeBy(1)
            runCurrent()
            assertNull(model.state.value.undoMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun newerUndoSurvivesTheOldDeadlineAndRestoresOnlyItsOwnItem() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val initial = (source.snapshot.value as AppResult.Success).value
        val milk = feedbackItem(id = "Milk")
        val bread = feedbackItem(id = "Bread")
        source.snapshot.value = AppResult.Success(initial.copy(items = listOf(milk, bread)))
        val store = ViewModelStore()
        val model = createViewModel(source = source).also { store.put("shopping", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            model.onRemoveItem(milk)
            model.onRemoveItemConfirmed()
            runCurrent()
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION / 2)
            model.onRemoveItem(bread)
            model.onRemoveItemConfirmed()
            runCurrent()
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION / 2)
            runCurrent()
            assertNotNull(model.state.value.undoMessage)
            model.onUndo()
            runCurrent()
            assertFalse(model.state.value.snapshot.items.first { it.id == bread.id }.removed)
            assertTrue(model.state.value.snapshot.items.first { it.id == milk.id }.removed)
            assertNull(model.state.value.undoMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun leavingWhileAnItemIsSavingCannotReviveItsOldUndo() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource().apply { deferItemWrites = true }
        val initial = (source.snapshot.value as AppResult.Success).value
        val milk = feedbackItem(id = "Milk")
        source.snapshot.value = AppResult.Success(initial.copy(items = listOf(milk)))
        val store = ViewModelStore()
        val model = createViewModel(source = source).also { store.put("shopping", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            model.onRemoveItem(milk)
            model.onRemoveItemConfirmed()
            runCurrent()
            model.onDismissUndo()
            source.itemWrites
                .getValue(milk.id)
                .complete(AppResult.Success(milk.copy(removed = true, version = 2)))
            runCurrent()
            assertNull(model.state.value.undoMessage)
            assertTrue(model.state.value.snapshot.items.single().removed)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun favouritesKeepCheckedAndRemovedItemsAndReuseThemWithoutAForm() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = ShoppingEntryFixtureDataSource()
        val initial = (source.snapshot.value as AppResult.Success).value
        val milk = feedbackItem(id = "Milk", checked = true, favorite = true)
        val bread = feedbackItem(id = "Bread", removed = true, favorite = true)
        val otherList = feedbackItem(id = "Trip snack", favorite = true).copy(listId = "holiday")
        source.snapshot.value =
            AppResult.Success(initial.copy(items = listOf(milk, bread, otherList)))
        val store = ViewModelStore()
        val model = createViewModel(source = source).also { store.put("shopping", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertEquals(listOf("Bread", "Milk"), model.state.value.favorites.map { it.item.name })
            model.onFavoritesVisibilityChanged(true)
            model.onReuseItem(bread)
            runCurrent()
            assertTrue(model.state.value.showFavorites)
            assertFalse(model.state.value.showAdd)
            assertEquals("Bread", model.state.value.uncheckedItems.single().name)
            assertTrue(model.state.value.favorites.first().alreadyOnList)
            model.onReuseItem(milk)
            runCurrent()
            assertEquals(
                setOf("Bread", "Milk"),
                model.state.value.uncheckedItems.map { it.name }.toSet(),
            )
            assertTrue(model.state.value.favorites.all { it.alreadyOnList })
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION / 2)
            model.onReuseItem(milk)
            advanceTimeBy(DEFAULT_ACTION_ALERT_DURATION / 2)
            runCurrent()
            assertNull(
                model.state.value.undoMessage,
                "Already-present favourites must not create another write/Undo window",
            )
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun tappingARowRenamesInPlaceAndTheKeyboardArrowOpensALineDirectlyBelow() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = keepSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val apples = viewModel.state.value.uncheckedItems.first()
            viewModel.onStartEditing(apples)
            runCurrent()
            assertEquals("apples", viewModel.state.value.editingItemId)
            assertEquals("apples", viewModel.state.value.editingName)
            viewModel.onEditingNameChanged("Green apples")
            viewModel.onEditingNext()
            runCurrent()
            assertNull(viewModel.state.value.editingItemId)
            assertEquals(
                "Green apples",
                viewModel.state.value.snapshot.items.first { it.id == "apples" }.name,
            )
            assertNull(viewModel.state.value.undoMessage, "The rename itself is the feedback")
            assertEquals("apples", viewModel.state.value.draftAfterId)
            assertEquals(
                listOf("apples", "shopping_draft", "bread"),
                viewModel.state.value.uncheckedRows.map { it.key },
            )
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun draftLinesChainBelowTheirAnchorAndSaveInTypingOrder() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = keepSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onStartEditing(viewModel.state.value.uncheckedItems.first())
            viewModel.onEditingNext()
            viewModel.onDraftNameChanged("Avocados")
            viewModel.onSubmitDraft()
            viewModel.onDraftNameChanged("Bananas")
            viewModel.onSubmitDraft()
            runCurrent()
            val rows = viewModel.state.value.uncheckedRows.map { it.key }
            assertEquals("apples", rows.first())
            assertEquals("bread", rows.last())
            assertEquals("shopping_draft", rows[3], "The cursor stays on the newest line")
            assertEquals(1, source.requests.size, "Lines are sent one at a time, in order")
            assertEquals("apples", source.insertAnchors.single())
            val avocados = source.requests.single().single()
            source.responses.send(
                AppResult.Success(
                    listOf(
                        avocados.copy(version = 1, position = 2),
                        bread.copy(position = 3),
                    ),
                ),
            )
            runCurrent()
            assertEquals(avocados.id, source.insertAnchors.last(), "The next line follows it")
            val bananas = source.requests.last().single()
            assertEquals("Bananas", bananas.name)
            source.responses.send(
                AppResult.Success(
                    listOf(bananas.copy(version = 1, position = 3), bread.copy(position = 4)),
                ),
            )
            runCurrent()
            assertEquals(
                listOf("apples", "Avocados", "Bananas", "bread"),
                viewModel.state.value.uncheckedItems.map { it.name },
            )
            viewModel.onSubmitDraft()
            assertNull(viewModel.state.value.draftAfterId, "An empty line closes the draft")
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun backspaceOnAnEmptyLineRemovesItAndEditsTheLineAbove() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = keepSource()).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val (first, second) = viewModel.state.value.uncheckedItems.take(2)
            viewModel.onStartEditing(second)
            viewModel.onEditingNameChanged("x")
            viewModel.onEditingDeleteEmpty()
            runCurrent()
            assertEquals(second.id, viewModel.state.value.editingItemId, "Text remains to erase")
            viewModel.onEditingNameChanged("")
            viewModel.onEditingDeleteEmpty()
            runCurrent()
            assertEquals(first.id, viewModel.state.value.editingItemId, "Focus moves up a line")
            assertEquals(first.name, viewModel.state.value.editingName)
            assertEquals(listOf(first.id), viewModel.state.value.uncheckedItems.map { it.id })
            // The removed row losing focus must not close the line now being edited.
            viewModel.onEditingFinished(second.id)
            assertEquals(first.id, viewModel.state.value.editingItemId)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun backspaceOnTheFirstEmptyLineEditsTheLineBelow() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = keepSource()).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val (first, second) = viewModel.state.value.uncheckedItems.take(2)
            viewModel.onStartEditing(first)
            viewModel.onEditingNameChanged("")
            viewModel.onEditingDeleteEmpty()
            runCurrent()
            assertEquals(second.id, viewModel.state.value.editingItemId)
            assertEquals(listOf(second.id), viewModel.state.value.uncheckedItems.map { it.id })
            assertTrue(viewModel.state.value.undoMessage != null, "Removal offers Undo")
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun backspaceOnAnEmptyNewLineReturnsToTheLineAbove() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = keepSource()).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val first = viewModel.state.value.uncheckedItems.first()
            viewModel.onStartEditing(first)
            viewModel.onEditingNext()
            runCurrent()
            assertEquals(first.id, viewModel.state.value.draftAfterId)
            viewModel.onDraftDeleteEmpty()
            runCurrent()
            assertNull(viewModel.state.value.draftAfterId)
            assertEquals(first.id, viewModel.state.value.editingItemId)
            viewModel.onEditingFinished(first.id)
            viewModel.onEntryDeleteEmpty()
            runCurrent()
            assertEquals(
                viewModel.state.value.uncheckedItems.last().id,
                viewModel.state.value.editingItemId,
                "Backspace in the empty add field edits the last item",
            )
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aBlankRenameRevertsWhenTheRowLosesFocus() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = keepSource()
        source.deferItemWrites = true
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val first = viewModel.state.value.uncheckedItems.first()
            viewModel.onStartEditing(first)
            viewModel.onEditingNameChanged("   ")
            viewModel.onEditingFinished(first.id)
            runCurrent()
            assertNull(viewModel.state.value.editingItemId)
            assertNull(viewModel.state.value.draftAfterId)
            assertTrue(source.itemWrites.isEmpty())
            assertEquals("apples", viewModel.state.value.uncheckedItems.first().name)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun movingUsesOneMenuEntryAndASheetOfTheOtherLists() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = keepSource()
        val store = ViewModelStore()
        try {
            val viewModel = createViewModel(source = source).also { store.put("shopping", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val apples = viewModel.state.value.uncheckedItems.first()
            viewModel.onMoveRequested(apples)
            assertEquals(apples, viewModel.state.value.movingItem)
            assertEquals(listOf("holiday"), viewModel.state.value.moveTargets.map { it.id })
            viewModel.onMoveItem(apples, "holiday")
            runCurrent()
            assertNull(viewModel.state.value.movingItem)
            assertEquals(
                "holiday",
                viewModel.state.value.snapshot.items.first { it.id == "apples" }.listId,
            )
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private val apples = feedbackItem(id = "apples").copy(position = 1)
    private val bread = feedbackItem(id = "bread").copy(position = 2)

    private fun keepSource(): ShoppingEntryFixtureDataSource =
        ShoppingEntryFixtureDataSource().apply {
            val original = (snapshot.value as AppResult.Success).value
            snapshot.value = AppResult.Success(original.copy(items = listOf(apples, bread)))
        }

    private fun feedbackItem(
        id: String,
        checked: Boolean = false,
        removed: Boolean = false,
        favorite: Boolean = false,
    ): ShoppingItemDomain =
        ShoppingItemDomain(
            id = id,
            name = id,
            quantity = "",
            category = "Produce",
            checked = checked,
            listId = "groceries",
            version = 1,
            removed = removed,
            favorite = favorite,
        )

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
            removeCompletedShoppingItemsUseCase =
                RemoveCompletedShoppingItemsUseCase(shoppingDataSource = source),
            deleteShoppingListUseCase = DeleteShoppingListUseCase(shoppingDataSource = source),
            trackAnalyticsEventUseCase =
                TrackAnalyticsEventUseCase(
                    analyticsDataSource = ShoppingAnalyticsFixtureDataSource(),
                ),
        )
}
