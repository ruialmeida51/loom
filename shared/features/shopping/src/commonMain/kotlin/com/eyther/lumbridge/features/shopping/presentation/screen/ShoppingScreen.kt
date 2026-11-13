package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import kotlinx.coroutines.flow.*

@Composable
fun ShoppingScreen(vm: ShoppingScreenViewModelContract) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerShoppingScreen(
        items = state.items,
        name = state.name,
        quantity = state.quantity,
        selectedCategory = state.category,
        showAdd = state.showAdd,
        showChecked = state.showChecked,
        busy = state.busy,
        error = state.error,
        onShowAdd = vm::showAdd,
        onName = vm::name,
        onQuantity = vm::quantity,
        onCategory = vm::category,
        onShowChecked = vm::showChecked,
        onAdd = vm::add,
        onToggle = vm::toggle,
        onRemove = vm::remove,
        onDismiss = vm::dismiss,
    )
}

@Composable
private fun InnerShoppingScreen(
    items: List<ShoppingItemDomain>,
    name: String,
    quantity: String,
    selectedCategory: String,
    showAdd: Boolean,
    showChecked: Boolean,
    busy: Boolean,
    error: String?,
    onShowAdd: (Boolean) -> Unit,
    onName: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onCategory: (String) -> Unit,
    onShowChecked: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val remaining = items.count { !it.checked }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
    ) {
        item {
            PageHeading(
                "ONE LESS THING TO REMEMBER",
                "The shopping list",
                "$remaining things left to pick up. Everyone’s on the same page.",
            )
        }
        item { ErrorNotice(error, onDismiss) }
        item { LoomButton("Add an item", { onShowAdd(true) }, Modifier.fillMaxWidth()) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(!showChecked, { onShowChecked(false) }, label = { Text("To pick up") })
                Spacer(Modifier.width(8.dp))
                FilterChip(showChecked, { onShowChecked(true) }, label = { Text("All items") })
            }
        }
        val visible = items.filter { showChecked || !it.checked }
        if (visible.isEmpty())
            item {
                EmptyNotice(
                    "All picked up.",
                    "Your list is clear. Add something whenever you think of it.",
                )
            }
        visible
            .groupBy { it.category }
            .forEach { (category, items) ->
                item {
                    Text(
                        category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(items, key = { it.id }) { item ->
                    LoomCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(item.checked, { onToggle(item) })
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    textDecoration =
                                        if (item.checked) TextDecoration.LineThrough
                                        else TextDecoration.None,
                                )
                                if (item.quantity.isNotBlank())
                                    Text(
                                        item.quantity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                            }
                            TextButton(onClick = { onRemove(item.id) }) {
                                Text("Remove", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
    }
    if (showAdd)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowAdd(false) },
            title = { Text("Add to the list") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        name,
                        onName,
                        label = { Text("What do we need?") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        quantity,
                        onQuantity,
                        label = { Text("Quantity or note") },
                        singleLine = true,
                    )
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    Column {
                        listOf("Produce", "Bakery", "Dairy & eggs", "Pantry", "Household")
                            .forEach { category ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selectedCategory == category,
                                        { onCategory(category) },
                                    )
                                    Text(category)
                                }
                            }
                    }
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = { LoomButton("Add item", onAdd, loading = busy) },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) { Text("Cancel") }
            },
        )
}
