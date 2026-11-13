package com.eyther.lumbridge.features.shopping.domain.model

import com.eyther.lumbridge.core.domain.*

data class ShoppingItemDomain(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
)
