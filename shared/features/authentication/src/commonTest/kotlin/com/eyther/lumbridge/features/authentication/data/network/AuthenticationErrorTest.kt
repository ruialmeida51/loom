package com.eyther.lumbridge.features.authentication.data.network

import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.mapper.toAuthenticationError
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import io.github.jan.supabase.auth.exception.AuthRestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class AuthenticationErrorTest {

    @Test
    fun sdkCodesBecomeTypedAuthenticationFailures() = runTest {
        val client = HttpClient(MockEngine { respond("", status = HttpStatusCode.BadRequest) })

        try {
            val response = client.get("https://loom.test/auth/v1/token")
            val invalidCredentials = AuthRestException("invalid_credentials", "opaque server text", response)
            val unconfirmedEmail = AuthRestException("email_not_confirmed", "opaque server text", response)
            val credentialsResult = safeApiCall<Unit>(mapError = Exception::toAuthenticationError) {
                throw invalidCredentials
            }
            val emailResult = safeApiCall<Unit>(mapError = Exception::toAuthenticationError) {
                throw unconfirmedEmail
            }

            val credentialsError = assertIs<AuthenticationError.InvalidCredentials>(
                assertIs<AppResult.Failure>(credentialsResult).error,
            )
            assertSame(invalidCredentials, credentialsError.cause)
            assertIs<AuthenticationError.EmailNotConfirmed>(assertIs<AppResult.Failure>(emailResult).error)

            val otherFailures = listOf(
                "email_address_invalid" to AuthenticationError.InvalidEmail::class,
                "email_address_not_authorized" to AuthenticationError.EmailDeliveryUnavailable::class,
                "over_email_send_rate_limit" to AuthenticationError.TooManyAttempts::class,
                "otp_expired" to AuthenticationError.VerificationCodeExpired::class,
            )
            otherFailures.forEach { (code, expected) ->
                val failure = AuthRestException(code, "opaque server text", response).toAuthenticationError()
                assertEquals(expected, failure::class)
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun unknownAuthCodesDoNotMatchDescriptions() = runTest {
        val client = HttpClient(MockEngine { respond("", status = HttpStatusCode.BadRequest) })

        try {
            val response = client.get("https://loom.test/auth/v1/token")
            val cause = AuthRestException("unknown_code", "invalid_credentials", response)

            assertIs<CommonError.Unknown>(cause.toAuthenticationError())
        } finally {
            client.close()
        }
    }
}
