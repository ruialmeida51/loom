package com.eyther.lumbridge.features.shopping.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

/** One row of the unchecked checklist, in display order. */
@Stable
sealed interface ShoppingRowUi {
    val key: String
    val indented: Boolean

    @Stable
    data class Item(val item: ShoppingItemDomain, override val indented: Boolean) : ShoppingRowUi {
        override val key: String = item.id
    }

    @Stable
    data class Pending(val entry: ShoppingEntryUi, override val indented: Boolean) : ShoppingRowUi {
        override val key: String = entry.item.id
    }

    /** The line being typed below another row. A constant key keeps focus while it moves. */
    @Stable
    data class Draft(override val indented: Boolean) : ShoppingRowUi {
        override val key: String = "shopping_draft"
    }
}
