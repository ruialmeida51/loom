package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toReceiptError(): AppError {
    if (this is AppError) return this
    if (this is RestException && statusCode == 404) return ReceiptError.NotFound(cause = this)
    if (this is PostgrestRestException && code == "P0001") {
        when (error) {
            "expenses.receipt_changed" -> return ReceiptError.ReceiptChanged(cause = this)
            "expenses.invalid_receipt" -> return ReceiptError.InvalidPhoto(cause = this)
        }
    }
    return toExpenseError()
}
