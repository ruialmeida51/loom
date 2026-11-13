package com.eyther.lumbridge.features.shopping.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class ShoppingError(cause: Throwable? = null) : AppError(cause) {

    class ListNameAlreadyExists(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidBatchSize(cause: Throwable? = null) : ShoppingError(cause)

    class ItemsAlreadySaved(cause: Throwable? = null) : ShoppingError(cause)

    class VersionRequired(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidItemName(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidQuantity(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidListOrCategory(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidListName(cause: Throwable? = null) : ShoppingError(cause)

    class ActiveListRequired(cause: Throwable? = null) : ShoppingError(cause)

    class AccessDenied(cause: Throwable? = null) : ShoppingError(cause)

    class ListChanged(cause: Throwable? = null) : ShoppingError(cause)

    class LastActiveList(cause: Throwable? = null) : ShoppingError(cause)

    class ListNotFound(cause: Throwable? = null) : ShoppingError(cause)

    class InvalidItem(cause: Throwable? = null) : ShoppingError(cause)

    class ItemChanged(cause: Throwable? = null) : ShoppingError(cause)

    class ItemNotFound(cause: Throwable? = null) : ShoppingError(cause)
}
