package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import kotlinx.collections.immutable.ImmutableList

/** Read-only preview; the owning Host opens the existing selected checklist. */
@Composable
fun ShoppingPreview(items: ImmutableList<ShoppingItemDomain>, onOpen: () -> Unit) {
    LoomCard(onClick = onOpen) {
        items.forEach { Text(it.name, style = MaterialTheme.typography.bodyLarge) }
    }
}
