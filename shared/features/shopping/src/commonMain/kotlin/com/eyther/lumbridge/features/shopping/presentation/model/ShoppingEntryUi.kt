package com.eyther.lumbridge.features.shopping.presentation.model

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

/**
 * An immediately submitted checklist row keeps its own retry identity while the next is typed.
 * [afterId] places a Keep-style line directly below another row; null appends.
 */
data class ShoppingEntryUi(
    val item: ShoppingItemDomain,
    val error: TextResource? = null,
    val afterId: String? = null,
) {
    val saving: Boolean = error == null
}
