package com.eyther.lumbridge.features.shopping.data.network.mapper

import com.eyther.lumbridge.features.shopping.data.network.model.request.ShoppingGroupItemRequest
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

internal fun ShoppingItemDomain.toGroupRequest(): ShoppingGroupItemRequest =
    ShoppingGroupItemRequest(
        id = id,
        listId = listId,
        name = name,
        quantity = quantity,
        category = category,
        checked = checked,
        favorite = favorite,
        removed = removed,
        version = version,
        parentId = parentId,
    )
