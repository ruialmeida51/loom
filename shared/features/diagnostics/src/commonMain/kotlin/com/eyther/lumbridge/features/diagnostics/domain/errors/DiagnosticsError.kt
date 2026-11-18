package com.eyther.lumbridge.features.diagnostics.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class DiagnosticsError(cause: Throwable? = null) : AppError(cause) {
    class Unavailable : DiagnosticsError()
}
