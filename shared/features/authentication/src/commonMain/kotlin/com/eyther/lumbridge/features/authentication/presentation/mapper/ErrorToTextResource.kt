package com.eyther.lumbridge.features.authentication.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.error_code_expired
import com.eyther.lumbridge.features.authentication.generated.resources.error_email_already_registered
import com.eyther.lumbridge.features.authentication.generated.resources.error_email_delivery_unavailable
import com.eyther.lumbridge.features.authentication.generated.resources.error_email_not_confirmed
import com.eyther.lumbridge.features.authentication.generated.resources.error_google_sign_in_cancelled
import com.eyther.lumbridge.features.authentication.generated.resources.error_google_sign_in_failed
import com.eyther.lumbridge.features.authentication.generated.resources.error_invalid_code
import com.eyther.lumbridge.features.authentication.generated.resources.error_invalid_credentials
import com.eyther.lumbridge.features.authentication.generated.resources.error_invalid_email
import com.eyther.lumbridge.features.authentication.generated.resources.error_invalid_registration
import com.eyther.lumbridge.features.authentication.generated.resources.error_missing_credentials
import com.eyther.lumbridge.features.authentication.generated.resources.error_missing_email
import com.eyther.lumbridge.features.authentication.generated.resources.error_terms_not_accepted
import com.eyther.lumbridge.features.authentication.generated.resources.error_too_many_attempts
import com.eyther.lumbridge.features.authentication.generated.resources.error_verification_failed
import com.eyther.lumbridge.features.authentication.generated.resources.error_weak_password

fun AppError.toAuthenticationTextResource(): TextResource = when (this) {
    is AuthenticationError.GoogleSignInFailed -> TextResource.Resource(Res.string.error_google_sign_in_failed)
    is AuthenticationError.GoogleSignInCancelled -> TextResource.Resource(Res.string.error_google_sign_in_cancelled)
    is AuthenticationError.TermsNotAccepted -> TextResource.Resource(Res.string.error_terms_not_accepted)
    is AuthenticationError.InvalidRegistration -> TextResource.Resource(Res.string.error_invalid_registration)
    is AuthenticationError.MissingCredentials -> TextResource.Resource(Res.string.error_missing_credentials)
    is AuthenticationError.MissingEmail -> TextResource.Resource(Res.string.error_missing_email)
    is AuthenticationError.InvalidEmail -> TextResource.Resource(Res.string.error_invalid_email)
    is AuthenticationError.InvalidVerificationCode -> TextResource.Resource(Res.string.error_invalid_code)
    is AuthenticationError.VerificationCodeExpired -> TextResource.Resource(Res.string.error_code_expired)
    is AuthenticationError.VerificationFailed -> TextResource.Resource(Res.string.error_verification_failed)
    is AuthenticationError.EmailDeliveryUnavailable -> TextResource.Resource(Res.string.error_email_delivery_unavailable)
    is AuthenticationError.InvalidCredentials -> TextResource.Resource(Res.string.error_invalid_credentials)
    is AuthenticationError.EmailNotConfirmed -> TextResource.Resource(Res.string.error_email_not_confirmed)
    is AuthenticationError.EmailAlreadyRegistered -> TextResource.Resource(Res.string.error_email_already_registered)
    is AuthenticationError.TooManyAttempts -> TextResource.Resource(Res.string.error_too_many_attempts)
    is AuthenticationError.WeakPassword -> TextResource.Resource(Res.string.error_weak_password)
    else -> toTextResource()
}
