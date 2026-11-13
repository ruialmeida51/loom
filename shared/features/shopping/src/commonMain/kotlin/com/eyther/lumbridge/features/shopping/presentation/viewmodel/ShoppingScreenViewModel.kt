package com.eyther.lumbridge.features.shopping.presentation.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.ObserveShoppingUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.ToggleShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingState
import kotlinx.coroutines.flow.*

class ShoppingScreenViewModel(
    observe: ObserveShoppingUseCase,
    private val addItem: AddShoppingItemUseCase,
    private val toggleItem: ToggleShoppingItemUseCase,
    private val removeItem: RemoveShoppingItemUseCase,
) : ShoppingScreenViewModelContract() {
    private data class InnerInputState(
        val name: String = "",
        val quantity: String = "",
        val category: String = "Produce",
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(ShoppingState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) {
                copy(name = it.name, quantity = it.quantity, category = it.category)
            }
        }
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            observe()
                .catch { error ->
                    mutableState.update {
                        it.copy(error = error.message ?: "Couldn’t load your shopping list.")
                    }
                }
                .onEach { items -> mutableState.update { it.copy(items = items) } }
        }
    }

    override fun showAdd(value: Boolean) {
        if (value) inputState.value = InnerInputState()
        mutableState.update { it.copy(showAdd = value) }
    }

    override fun name(value: String) {
        inputState.update { it.copy(name = value) }
    }

    override fun quantity(value: String) {
        inputState.update { it.copy(quantity = value) }
    }

    override fun category(value: String) {
        inputState.update { it.copy(category = value) }
    }

    override fun showChecked(value: Boolean) {
        mutableState.update { it.copy(showChecked = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun add() {
        if (mutableState.value.busy) return
        val input = inputState.value
        val item = ShoppingItemDomain(newId(), input.name, input.quantity, input.category, false)
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = addItem(item)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(
                            busy = false,
                            showAdd = false,
                            name = "",
                            quantity = "",
                            items = old.items.filterNot { it.id == item.id } + item,
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun toggle(item: ShoppingItemDomain) {
        mutableState.update { old ->
            old.copy(
                items =
                    old.items.map {
                        if (it.id == item.id) it.copy(checked = !item.checked) else it
                    },
            )
        }
        launch {
            val result = toggleItem(item.id, !item.checked)
            if (result is AppResult.Failure)
                mutableState.update { old ->
                    old.copy(
                        error = result.message,
                        items = old.items.map { if (it.id == item.id) item else it },
                    )
                }
        }
    }

    override fun remove(id: String) {
        launch {
            when (val result = removeItem(id)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(items = old.items.filterNot { it.id == id })
                    }
                is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
            }
        }
    }
}
