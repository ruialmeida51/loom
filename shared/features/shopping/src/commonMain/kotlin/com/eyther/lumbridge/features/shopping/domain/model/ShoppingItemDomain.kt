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
        requireDomain(name.trim().length in 1..MAXIMUM_NAME_LENGTH) {
            ShoppingError.InvalidItemName()
        }
        requireDomain(quantity.length <= 40) { ShoppingError.InvalidQuantity() }
        requireDomain(category.length in 1..40 && listId.isNotBlank()) {
            ShoppingError.InvalidListOrCategory()
        }
        return copy(name = name.trim(), quantity = quantity.trim())
    }

    /** The name with any links split out, for rows that open them. */
    fun nameParts(): List<ShoppingNamePartDomain> = ShoppingNamePartDomain.parse(text = name)

    /** The name with each link shown as its site, for one-line previews and messages. */
    fun displayName(): String =
        nameParts().joinToString(separator = "") { part ->
            when (part) {
                is ShoppingNamePartDomain.Text -> part.value
                is ShoppingNamePartDomain.Link -> part.label
            }
        }

    companion object {
        /** Long enough for a pasted product page; matches the server's limit. */
        const val MAXIMUM_NAME_LENGTH = 500
    }
}
