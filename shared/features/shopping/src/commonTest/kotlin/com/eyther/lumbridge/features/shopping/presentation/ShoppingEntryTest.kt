package com.eyther.lumbridge.features.shopping.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingAnalyticsFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingEntryFixtureDataSource
import com.eyther.lumbridge.features.shopping.data.fixtures.ShoppingSelectionFixtureDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
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
    fun failedSubmissionRetainsItsDraftAndIdempotencyKey() = runTest {
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
            assertEquals("Milk", viewModel.state.value.inlineName)
            assertNotNull(viewModel.state.value.inlineError)
            viewModel.onAddInlineItem()
            runCurrent()
            assertEquals(source.requests[0].single().id, source.requests[1].single().id)
            source.responses.send(
                AppResult.Success(source.requests.last().map { it.copy(version = 1) }),
            )
            runCurrent()
            assertEquals("", viewModel.state.value.inlineName)
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
            assertEquals("Bread", viewModel.state.value.inlineName)
            viewModel.onAddInlineItem()
            runCurrent()
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
            trackAnalyticsEventUseCase =
                TrackAnalyticsEventUseCase(
                    analyticsDataSource = ShoppingAnalyticsFixtureDataSource(),
                ),
        )
}
