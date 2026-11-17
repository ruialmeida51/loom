package com.eyther.lumbridge.features.shopping.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.DeleteShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveCompletedShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SelectShoppingListUseCase
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.completed_deleted
import com.eyther.lumbridge.features.shopping.generated.resources.favorite_updated
import com.eyther.lumbridge.features.shopping.generated.resources.item_moved
import com.eyther.lumbridge.features.shopping.generated.resources.item_removed
import com.eyther.lumbridge.features.shopping.generated.resources.item_restored
import com.eyther.lumbridge.features.shopping.generated.resources.item_updated
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingEntryUi
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingState
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingScreenViewModel(
    private val getShoppingStreamUseCase: GetShoppingStreamUseCase,
    private val getShoppingSelectionStreamUseCase: GetShoppingSelectionStreamUseCase,
    private val selectShoppingListUseCase: SelectShoppingListUseCase,
    private val addShoppingItemsUseCase: AddShoppingItemsUseCase,
    private val saveShoppingItemUseCase: SaveShoppingItemUseCase,
    private val saveShoppingListUseCase: SaveShoppingListUseCase,
    private val removeCompletedShoppingItemsUseCase: RemoveCompletedShoppingItemsUseCase,
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase,
    private val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
) : ShoppingScreenViewModelContract() {

    private data class InnerInputState(
        val name: String = "",
        val inlineName: String = "",
        val quantity: String = "",
        val category: String = "Produce",
        val listName: String = "",
    )

    private data class UndoChange(
        val before: ShoppingItemDomain,
        val afterVersion: Long,
    )

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private val entryRequests = Channel<ShoppingItemDomain>(capacity = Channel.UNLIMITED)
    private var pendingItemUpdates: Map<String, ShoppingItemDomain> = emptyMap()
    private var acknowledgedItems: Map<String, ShoppingItemDomain> = emptyMap()
    private var editingItem: ShoppingItemDomain? = null
    private var editingList: ShoppingListDomain? = null
    private var requestIds: List<String> = emptyList()
    private var newListId: String = newId()
    private var feedbackJob: Job? = null
    private var undoChange: UndoChange? = null

    private val mutableState =
        MutableStateFlow(
                ShoppingState(selectedListId = getShoppingSelectionStreamUseCase().value.orEmpty()),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        name = input.name,
                        inlineName = input.inlineName,
                        quantity = input.quantity,
                        category = input.category,
                        listName = input.listName,
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                getShoppingSelectionStreamUseCase().updateState(state) { id ->
                    copy(
                        selectedListId =
                            if (snapshot.activeLists.isEmpty()) id.orEmpty()
                            else snapshot.resolveActiveListId(id.orEmpty()),
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getShoppingStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                            .map { result ->
                                when (result) {
                                    is AppResult.Failure -> result
                                    is AppResult.Success -> {
                                        val remoteItems = result.value.items.associateBy { it.id }
                                        acknowledgedItems = acknowledgedItems.filter { (id, item) ->
                                            result.value.lists.any { it.id == item.listId } &&
                                                (remoteItems[id]?.version ?: -1) < item.version
                                        }
                                        val retainedIds = acknowledgedItems.keys
                                        val merged =
                                            result.value.copy(
                                                items =
                                                    result.value.items.filterNot {
                                                        it.id in retainedIds
                                                    } + acknowledgedItems.values,
                                            )
                                        AppResult.Success(
                                            merged.copy(
                                                items =
                                                    merged.items.map { remote ->
                                                        pendingItemUpdates[remote.id]?.takeIf {
                                                            it.version >= remote.version
                                                        } ?: remote
                                                    },
                                            ),
                                        )
                                    }
                                }
                            }
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    snapshot = result.value,
                                    loading = false,
                                    refreshing = false,
                                    selectedListId =
                                        result.value.resolveActiveListId(selectedListId),
                                    loadError = null,
                                )

                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    loadError = result.error.toShoppingTextResource(),
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onCompletedSelectionChanged(selecting: Boolean) {
        if (state.value.deletingCompleted) return
        if (selecting) onDismissUndo()
        mutableState.update { oldState ->
            oldState.copy(
                selectingCompleted = selecting,
                selectedCompletedIds = persistentSetOf(),
                showChecked = if (selecting) true else oldState.showChecked,
            )
        }
    }

    override fun onCompletedItemSelected(id: String) {
        if (state.value.deletingCompleted) return
        mutableState.update { oldState ->
            oldState.copy(
                selectedCompletedIds =
                    if (id in oldState.selectedCompletedIds)
                        oldState.selectedCompletedIds.remove(id)
                    else oldState.selectedCompletedIds.add(id),
            )
        }
    }

    override fun onSelectAllCompleted() {
        if (state.value.deletingCompleted) return
        mutableState.update { oldState ->
            oldState.copy(
                selectedCompletedIds =
                    if (oldState.allCompletedSelected) persistentSetOf()
                    else oldState.completedItems.map { it.id }.toPersistentSet(),
            )
        }
    }

    override fun onDeleteCompletedRequested() {
        if (state.value.selectedCompletedItems.isNotEmpty())
            mutableState.update { oldState ->
                oldState.copy(
                    confirmingCompletedDeletion = true,
                    pendingCompletedDeletion = oldState.selectedCompletedItems,
                )
            }
    }

    override fun onDeleteCompletedCancelled() {
        if (!state.value.deletingCompleted)
            mutableState.update { oldState -> oldState.copy(confirmingCompletedDeletion = false) }
    }

    override fun onDeleteCompletedConfirmed() {
        if (state.value.deletingCompleted) return
        val items = state.value.pendingCompletedDeletion
        if (items.isEmpty()) return
        mutableState.update { oldState -> oldState.copy(deletingCompleted = true, error = null) }
        launch {
            when (val result = removeCompletedShoppingItemsUseCase(items = items)) {
                is AppResult.Success -> {
                    val savedIds = result.value.map { it.id }.toSet()
                    acknowledgedItems = acknowledgedItems + result.value.associateBy { it.id }
                    mutableState.update { oldState ->
                        oldState.copy(
                            snapshot =
                                oldState.snapshot.copy(
                                    items =
                                        oldState.snapshot.items.filterNot { it.id in savedIds } +
                                            result.value,
                                ),
                            deletingCompleted = false,
                            selectingCompleted = false,
                            confirmingCompletedDeletion = false,
                            selectedCompletedIds = persistentSetOf(),
                            successMessage =
                                TextResource.Resource(
                                    Res.string.completed_deleted,
                                    args = listOf(result.value.size),
                                ),
                        )
                    }
                    feedbackJob?.cancel()
                    feedbackJob = launch {
                        delay(DEFAULT_ALERT_DURATION)
                        mutableState.update { oldState -> oldState.copy(successMessage = null) }
                    }
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            deletingCompleted = false,
                            confirmingCompletedDeletion = false,
                            error = result.error.toShoppingTextResource(),
                        )
                    }
            }
        }
    }

    override fun onInlineNameChanged(value: String) {
        inputState.update { oldState -> oldState.copy(inlineName = value) }
        mutableState.update { oldState -> oldState.copy(inlineError = null) }
    }

    init {
        viewModelScope.launch {
            // Preserve Enter order even when a network round trip takes longer than typing.
            for (item in entryRequests) submitInlineItem(item = item)
        }
    }

    override fun onAddInlineItem() {
        val name = inputState.value.inlineName
        val listId = state.value.selectedList?.id ?: return
        if (name.isBlank() || state.value.busy) return
        val item =
            ShoppingItemDomain(
                id = newId(),
                name = name,
                quantity = "",
                category = "Other",
                checked = false,
                listId = listId,
            )
        inputState.update { oldState -> oldState.copy(inlineName = "") }
        mutableState.update { oldState ->
            oldState.copy(
                inlineName = "",
                inlineError = null,
                entryRevision = oldState.entryRevision + 1,
                pendingEntries = oldState.pendingEntries + ShoppingEntryUi(item = item),
            )
        }
        entryRequests.trySend(item)
    }

    override fun onRetryEntry(id: String) {
        val entry =
            state.value.pendingEntries.firstOrNull { it.item.id == id && !it.saving } ?: return
        mutableState.update { oldState ->
            oldState.copy(
                pendingEntries =
                    oldState.pendingEntries.map {
                        if (it.item.id == id) it.copy(error = null) else it
                    },
            )
        }
        entryRequests.trySend(entry.item)
    }

    override fun onDiscardEntry(id: String) {
        mutableState.update { oldState ->
            oldState.copy(
                pendingEntries =
                    oldState.pendingEntries.filterNot { it.item.id == id && !it.saving },
            )
        }
    }

    private suspend fun submitInlineItem(item: ShoppingItemDomain) {
        when (val result = addShoppingItemsUseCase(items = listOf(item))) {
            is AppResult.Success -> {
                acknowledgedItems = acknowledgedItems + result.value.associateBy { it.id }
                val savedIds = result.value.map { it.id }.toSet()
                mutableState.update { oldState ->
                    oldState.copy(
                        pendingEntries =
                            oldState.pendingEntries.filterNot { it.item.id == item.id },
                        snapshot =
                            oldState.snapshot.copy(
                                items =
                                    oldState.snapshot.items.filterNot { it.id in savedIds } +
                                        result.value,
                            ),
                    )
                }
                trackAnalyticsEventUseCase(
                    event =
                        AnalyticsEventDomain.ActionCompleted(
                            action = AnalyticsActionDomain.SHOPPING_ITEMS_ADDED,
                        ),
                )
            }
            is AppResult.Failure ->
                mutableState.update { oldState ->
                    oldState.copy(
                        pendingEntries =
                            oldState.pendingEntries.map {
                                if (it.item.id == item.id)
                                    it.copy(error = result.error.toShoppingTextResource())
                                else it
                            },
                    )
                }
        }
    }

    override fun onDismissUndo() {
        undoChange = null
        mutableState.update { oldState -> oldState.copy(undoMessage = null) }
    }

    override fun onListSelected(id: String) {
        mutableState.update { oldState ->
            oldState.copy(
                selectedListId = id,
                selectingCompleted = false,
                selectedCompletedIds = persistentSetOf(),
                showChecked = false,
                showLists = false,
                inlineError = null,
            )
        }
        launch {
            val result = selectShoppingListUseCase(id = id)
            if (result is AppResult.Failure)
                mutableState.update { oldState ->
                    oldState.copy(error = result.error.toShoppingTextResource())
                }
        }
    }

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return

        if (value) {
            editingItem = null
            requestIds = emptyList()
            inputState.update { oldState ->
                oldState.copy(name = "", quantity = "", category = "Other")
            }
        }

        mutableState.update { oldState ->
            oldState.copy(
                showAdd = value,
                editing = if (value) false else oldState.editing,
                formError = null,
            )
        }
    }

    override fun onEditItem(item: ShoppingItemDomain) {
        editingItem = item
        inputState.update { oldState ->
            oldState.copy(name = item.name, quantity = item.quantity, category = item.category)
        }

        mutableState.update { oldState ->
            oldState.copy(showAdd = true, editing = true, formError = null)
        }
    }

    override fun onItemNameChanged(value: String) {
        if (value != inputState.value.name) requestIds = emptyList()

        inputState.update { oldState ->
            oldState.copy(name = value)
        }
    }

    override fun onQuantityChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(quantity = value)
        }
    }

    override fun onCategoryChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(category = value)
        }
    }

    override fun onCheckedItemsVisibilityChanged(value: Boolean) {
        mutableState.update { oldState ->
            oldState.copy(showChecked = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true) }
        onDismissError()
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onSaveItem() {
        if (state.value.busy) return
        val input = inputState.value
        val before = editingItem

        if (before != null) {
            saveItem(
                item =
                    before.copy(
                        name = input.name,
                        quantity = input.quantity,
                        category = input.category,
                    ),
                message = TextResource.Resource(Res.string.item_updated),
                closeForm = true,
            )
            return
        }

        val names = input.name.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (requestIds.size != names.size) requestIds = names.map { newId() }
        val items = names.mapIndexed { index, name ->
            ShoppingItemDomain(
                id = requestIds[index],
                name = name,
                quantity = input.quantity,
                category = input.category,
                checked = false,
                listId = state.value.selectedListId,
            )
        }

        mutableState.update { oldState ->
            oldState.copy(busy = true, formError = null)
        }

        launch {
            val result = addShoppingItemsUseCase(items)
            if (result is AppResult.Success) {
                trackAnalyticsEventUseCase(
                    event =
                        AnalyticsEventDomain.ActionCompleted(
                            action = AnalyticsActionDomain.SHOPPING_ITEMS_ADDED,
                        ),
                )
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val addedIds = result.value.map { it.id }.toSet()

                        oldState.copy(
                            busy = false,
                            showAdd = false,
                            snapshot =
                                oldState.snapshot.copy(
                                    items =
                                        oldState.snapshot.items.filterNot { it.id in addedIds } +
                                            result.value,
                                ),
                        )
                    }

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toShoppingTextResource(),
                        )
                }
            }
        }
    }

    override fun onToggleItem(item: ShoppingItemDomain) {
        saveItem(
            item = item.copy(checked = !item.checked),
            message = TextResource.Resource(Res.string.item_updated),
        )
    }

    override fun onRemoveItem(item: ShoppingItemDomain) {
        if (state.value.busy || item.id in state.value.savingItemIds) return
        mutableState.update { oldState ->
            oldState.copy(deletingItem = item, itemDeleteError = null)
        }
    }

    override fun onRemoveItemCancelled() {
        if (state.value.deletingSingleItem) return
        mutableState.update { oldState ->
            oldState.copy(deletingItem = null, itemDeleteError = null)
        }
    }

    override fun onRemoveItemConfirmed() {
        val item = state.value.deletingItem ?: return
        saveItem(
            item = item.copy(removed = true),
            message = TextResource.Resource(Res.string.item_removed),
        )
    }

    override fun onToggleFavorite(item: ShoppingItemDomain) {
        saveItem(
            item = item.copy(favorite = !item.favorite),
            message = TextResource.Resource(Res.string.favorite_updated),
        )
    }

    override fun onMoveItem(item: ShoppingItemDomain, listId: String) {
        saveItem(
            item = item.copy(listId = listId),
            message = TextResource.Resource(Res.string.item_moved),
        )
    }

    override fun onUndo() {
        val undo = undoChange ?: return
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = saveShoppingItemUseCase(undo.before.copy(version = undo.afterVersion))
            undoChange = null

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            undoMessage = null,
                            snapshot =
                                oldState.snapshot.copy(
                                    items =
                                        oldState.snapshot.items.map { item ->
                                            if (item.id == result.value.id) result.value else item
                                        },
                                ),
                        )

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            undoMessage = null,
                            error = result.error.toShoppingTextResource(),
                        )
                }
            }
        }
    }

    override fun onReuseItem(item: ShoppingItemDomain) {
        val existing =
            state.value.snapshot.findReusableItem(
                listId = state.value.selectedListId,
                name = item.name,
            )

        if (existing != null) {
            saveItem(
                item = existing.copy(checked = false, removed = false),
                message = TextResource.Resource(Res.string.item_restored),
            )
        } else {
            onAddSheetVisibilityChanged(true)
            inputState.value =
                InnerInputState(
                    name = item.name,
                    quantity = item.quantity,
                    category = item.category,
                )
        }
    }

    override fun onListsVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(showLists = value, formError = null)
        }
    }

    override fun onOpenListForm(list: ShoppingListDomain?) {
        editingList = list
        newListId = list?.id ?: newId()

        inputState.update { oldState ->
            oldState.copy(listName = list?.name.orEmpty())
        }
        mutableState.update { oldState ->
            oldState.copy(showLists = false, showListForm = true, formError = null)
        }
    }

    override fun onCloseListForm() {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(showListForm = false, formError = null)
        }
    }

    override fun onListNameChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(listName = value)
        }
    }

    override fun onSaveList() {
        val list = editingList ?: ShoppingListDomain(id = newListId, name = "")

        saveList(list.copy(name = inputState.value.listName))
    }

    override fun onArchiveList(list: ShoppingListDomain) {
        saveList(list.copy(archived = !list.archived))
    }

    override fun onDeleteListRequested(list: ShoppingListDomain) {
        if (state.value.busy || state.value.addingInline || state.value.savingItemIds.isNotEmpty())
            return
        mutableState.update { oldState -> oldState.copy(deletingList = list, formError = null) }
    }

    override fun onCancelDeleteList() {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(deletingList = null) }
    }

    override fun onConfirmDeleteList() {
        val list = state.value.deletingList ?: return
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, formError = null) }
        launch {
            when (val result = deleteShoppingListUseCase(list = list)) {
                is AppResult.Success -> {
                    acknowledgedItems = acknowledgedItems.filterValues { it.listId != list.id }
                    undoChange = null
                    mutableState.update { oldState ->
                        val snapshot =
                            oldState.snapshot.copy(
                                lists = oldState.snapshot.lists.filterNot { it.id == list.id },
                                items = oldState.snapshot.items.filterNot { it.listId == list.id },
                            )
                        oldState.copy(
                            snapshot = snapshot,
                            selectedListId = snapshot.resolveActiveListId(oldState.selectedListId),
                            deletingList = null,
                            pendingEntries =
                                oldState.pendingEntries.filterNot { it.item.listId == list.id },
                            busy = false,
                            showLists = snapshot.activeLists.isNotEmpty(),
                            undoMessage = null,
                        )
                    }
                    state.value.selectedListId
                        .takeIf { it.isNotBlank() }
                        ?.let { selectShoppingListUseCase(id = it) }
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            deletingList = null,
                            formError = result.error.toShoppingTextResource(),
                        )
                    }
            }
        }
    }

    private fun saveItem(
        item: ShoppingItemDomain,
        message: TextResource,
        closeForm: Boolean = false,
    ) {
        if (state.value.busy || item.id in state.value.savingItemIds) return
        val before = state.value.snapshot.items.firstOrNull { it.id == item.id } ?: return
        pendingItemUpdates = pendingItemUpdates + (item.id to item)
        mutableState.update { oldState ->
            oldState.copy(
                busy = closeForm,
                savingItemIds = oldState.savingItemIds.add(item.id),
                error = null,
                formError = null,
                undoMessage = null,
                itemDeleteError = null,
                snapshot =
                    oldState.snapshot.copy(
                        items =
                            oldState.snapshot.items.map {
                                if (it.id == item.id) item else it
                            },
                    ),
            )
        }
        launch {
            val result = saveShoppingItemUseCase(item = item)
            pendingItemUpdates = pendingItemUpdates - item.id
            when (result) {
                is AppResult.Success -> {
                    acknowledgedItems = acknowledgedItems + (result.value.id to result.value)
                    undoChange = UndoChange(before = before, afterVersion = result.value.version)
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = if (closeForm) false else oldState.busy,
                            savingItemIds = oldState.savingItemIds.remove(item.id),
                            showAdd = if (closeForm) false else oldState.showAdd,
                            undoMessage = message,
                            deletingItem =
                                if (item.removed && oldState.deletingItem?.id == item.id) null
                                else oldState.deletingItem,
                            snapshot =
                                oldState.snapshot.copy(
                                    items =
                                        oldState.snapshot.items.map { existing ->
                                            if (
                                                existing.id == item.id &&
                                                    existing.version <= result.value.version
                                            )
                                                result.value
                                            else existing
                                        },
                                ),
                        )
                    }
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = if (closeForm) false else oldState.busy,
                            savingItemIds = oldState.savingItemIds.remove(item.id),
                            error =
                                if (closeForm || oldState.deletingItem?.id == item.id)
                                    oldState.error
                                else result.error.toShoppingTextResource(),
                            itemDeleteError =
                                if (oldState.deletingItem?.id == item.id)
                                    result.error.toShoppingTextResource()
                                else oldState.itemDeleteError,
                            formError =
                                if (closeForm) result.error.toShoppingTextResource()
                                else oldState.formError,
                            snapshot =
                                oldState.snapshot.copy(
                                    items =
                                        oldState.snapshot.items.map { existing ->
                                            if (
                                                existing.id == item.id &&
                                                    existing.version <= before.version
                                            )
                                                before
                                            else existing
                                        },
                                ),
                        )
                    }
            }
        }
    }

    private fun saveList(list: ShoppingListDomain) {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, formError = null)
        }

        launch {
            val result = saveShoppingListUseCase(list)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val snapshot =
                            oldState.snapshot.copy(
                                lists =
                                    oldState.snapshot.lists.filterNot { it.id == list.id } +
                                        result.value,
                            )

                        oldState.copy(
                            busy = false,
                            showListForm = false,
                            selectedListId =
                                snapshot.resolveActiveListId(preferredId = result.value.id),
                            snapshot = snapshot,
                        )
                    }

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toShoppingTextResource(),
                        )
                }
            }
            if (result is AppResult.Success) {
                val selectionResult = selectShoppingListUseCase(id = result.value.id)
                if (selectionResult is AppResult.Failure)
                    mutableState.update { oldState ->
                        oldState.copy(error = selectionResult.error.toShoppingTextResource())
                    }
            }
        }
    }
}
