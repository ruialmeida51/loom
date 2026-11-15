package com.eyther.lumbridge.features.account.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class AccountError(cause: Throwable? = null) : AppError(cause) {
    class InvalidNickname(cause: Throwable? = null) : AccountError(cause)
}
