package com.eyther.lumbridge.core.domain.errors

sealed class CommonError(cause: Throwable? = null) : AppError(cause) {

    class NoConnection(cause: Throwable? = null) : CommonError(cause)

    class NotAuthenticated(cause: Throwable? = null) : CommonError(cause)

    class AccessDenied(cause: Throwable? = null) : CommonError(cause)

    class NotFound(cause: Throwable? = null) : CommonError(cause)

    class ServiceUnavailable(cause: Throwable? = null) : CommonError(cause)

    class InvalidResponse(cause: Throwable? = null) : CommonError(cause)

    class LocalStorage(cause: Throwable? = null) : CommonError(cause)

    class Unknown(cause: Throwable? = null) : CommonError(cause)
}
