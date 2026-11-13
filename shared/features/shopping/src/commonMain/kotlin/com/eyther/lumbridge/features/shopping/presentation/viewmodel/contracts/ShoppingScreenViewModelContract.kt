package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import kotlinx.coroutines.flow.*

data class ShoppingState(
    val items: List<ShoppingItemDomain> = emptyList(),
    val name: String = "",
    val quantity: String = "",
    val category: String = "Produce",
    val showAdd: Boolean = false,
    val showChecked: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class ShoppingScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<ShoppingState>

    abstract fun showAdd(value: Boolean)

    abstract fun name(value: String)

    abstract fun quantity(value: String)

    abstract fun category(value: String)

    abstract fun showChecked(value: Boolean)

    abstract fun add()

    abstract fun toggle(item: ShoppingItemDomain)

    abstract fun remove(id: String)

    abstract fun dismiss()
}
