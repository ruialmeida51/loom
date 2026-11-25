package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain

/** The selected shopping list and what is still to pick up on it. */
data class HomeShoppingDomain(
    val list: ShoppingListDomain?,
    val items: List<ShoppingItemDomain>,
)
