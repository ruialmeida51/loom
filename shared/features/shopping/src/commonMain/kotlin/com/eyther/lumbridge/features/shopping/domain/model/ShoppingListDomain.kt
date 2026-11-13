package com.eyther.lumbridge.features.shopping.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError

data class ShoppingListDomain(
    val id: String,
    val name: String,
    val archived: Boolean = false,
    val version: Long = 0,
) {

    fun validated(): ShoppingListDomain {
        requireDomain(name.trim().length in 1..60) {
            ShoppingError.InvalidListName()
        }
        return copy(name = name.trim())
    }
}
