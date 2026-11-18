package com.eyther.lumbridge.features.shopping.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError

data class ShoppingItemDomain(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
    val listId: String,
    val version: Long = 0,
    val removed: Boolean = false,
    val favorite: Boolean = false,
    val position: Long = 0,
    val parentId: String? = null,
) {

    fun validated(): ShoppingItemDomain {
        requireDomain(name.trim().length in 1..120) {
            ShoppingError.InvalidItemName()
        }
        requireDomain(quantity.length <= 40) { ShoppingError.InvalidQuantity() }
        requireDomain(category.length in 1..40 && listId.isNotBlank()) {
            ShoppingError.InvalidListOrCategory()
        }
        return copy(name = name.trim(), quantity = quantity.trim())
    }
}
