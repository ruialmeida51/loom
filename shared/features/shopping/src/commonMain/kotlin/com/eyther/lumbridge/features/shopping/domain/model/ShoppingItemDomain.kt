package com.eyther.lumbridge.features.shopping.domain.model

data class ShoppingItemDomain(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
)
