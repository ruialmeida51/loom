package com.eyther.lumbridge.features.authentication.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class AuthenticationError(cause: Throwable? = null) : AppError(cause) {

    class InvalidRegistration(cause: Throwable? = null) : AuthenticationError(cause)

    class MissingCredentials(cause: Throwable? = null) : AuthenticationError(cause)

    class InvalidCredentials(cause: Throwable? = null) : AuthenticationError(cause)

    class EmailNotConfirmed(cause: Throwable? = null) : AuthenticationError(cause)

    class EmailAlreadyRegistered(cause: Throwable? = null) : AuthenticationError(cause)

    class TooManyAttempts(cause: Throwable? = null) : AuthenticationError(cause)

    class WeakPassword(cause: Throwable? = null) : AuthenticationError(cause)
}
