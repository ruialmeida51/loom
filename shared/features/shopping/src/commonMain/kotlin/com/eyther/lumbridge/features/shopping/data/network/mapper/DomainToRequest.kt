package com.eyther.lumbridge.features.shopping.data.network.mapper

import com.eyther.lumbridge.features.shopping.data.network.model.request.SaveShoppingItemRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.SaveShoppingListRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.ShoppingItemRequest
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain

internal fun ShoppingItemDomain.toAddRequest(): ShoppingItemRequest = ShoppingItemRequest(
    id = id,
    name = name,
    quantity = quantity,
    category = category,
    listId = listId,
)

internal fun ShoppingItemDomain.toRequest(householdId: String): SaveShoppingItemRequest = SaveShoppingItemRequest(
    householdId = householdId,
    id = id,
    name = name,
    quantity = quantity,
    category = category,
    checked = checked,
    listId = listId,
    version = version,
    removed = removed,
    favorite = favorite,
)

internal fun ShoppingListDomain.toRequest(householdId: String): SaveShoppingListRequest = SaveShoppingListRequest(
    householdId = householdId,
    id = id,
    name = name,
    archived = archived,
    version = version,
    icon = icon.key,
)
