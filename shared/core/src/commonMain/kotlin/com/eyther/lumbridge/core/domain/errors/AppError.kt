package com.eyther.lumbridge.core.domain.errors

abstract class AppError(cause: Throwable? = null) : Exception(cause)
