package com.eyther.lumbridge.features.expenses.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class SettlementError(cause: Throwable? = null) : AppError(cause) {
    class Changed(cause: Throwable? = null) : SettlementError(cause)

    class PermissionDenied(cause: Throwable? = null) : SettlementError(cause)
}
