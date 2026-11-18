package com.eyther.lumbridge.features.expenses.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class ReceiptError(cause: Throwable? = null) : AppError(cause) {
    class NotFound(cause: Throwable? = null) : ReceiptError(cause)

    class InvalidPhoto(cause: Throwable? = null) : ReceiptError(cause)

    class PhotoTooLarge(cause: Throwable? = null) : ReceiptError(cause)

    class PhotoUnavailable(cause: Throwable? = null) : ReceiptError(cause)

    class ReceiptChanged(cause: Throwable? = null) : ReceiptError(cause)
}
