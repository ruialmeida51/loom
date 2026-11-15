package com.eyther.lumbridge.features.shopping.data.network.mapper

import com.eyther.lumbridge.features.shopping.data.network.model.response.ShoppingItemResponse
import com.eyther.lumbridge.features.shopping.data.network.model.response.ShoppingListResponse
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain

internal fun ShoppingItemResponse.toDomain(): ShoppingItemDomain =
    ShoppingItemDomain(
        id = id,
        name = name,
        quantity = quantity,
        category = category,
        checked = checked,
        listId = listId,
        version = version,
        removed = removed,
        favorite = favorite,
        position = position,
    )

internal fun ShoppingListResponse.toDomain(): ShoppingListDomain =
    ShoppingListDomain(
        id = id,
        name = name,
        archived = archived,
        version = version,
    )
