package com.eyther.lumbridge.core.domain.errors

inline fun requireDomain(value: Boolean, error: () -> AppError) {
    if (!value) throw error()
}
