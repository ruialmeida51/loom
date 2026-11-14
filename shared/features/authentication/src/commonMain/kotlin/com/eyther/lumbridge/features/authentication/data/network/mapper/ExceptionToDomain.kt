package com.eyther.lumbridge.features.authentication.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException

internal fun Exception.toAuthenticationError(): AppError {
    if (this !is AuthRestException) return toAppError()

    return when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AuthenticationError.InvalidCredentials(cause = this)
        AuthErrorCode.EmailNotConfirmed -> AuthenticationError.EmailNotConfirmed(cause = this)
        AuthErrorCode.EmailAddressInvalid -> AuthenticationError.InvalidEmail(cause = this)
        AuthErrorCode.OtpExpired -> AuthenticationError.VerificationCodeExpired(cause = this)
        AuthErrorCode.EmailAddressNotAuthorized, AuthErrorCode.EmailProviderDisabled ->
            AuthenticationError.EmailDeliveryUnavailable(cause = this)
        AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists ->
            AuthenticationError.EmailAlreadyRegistered(cause = this)
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit ->
            AuthenticationError.TooManyAttempts(cause = this)
        AuthErrorCode.WeakPassword -> AuthenticationError.WeakPassword(cause = this)
        else -> toAppError()
    }
}
