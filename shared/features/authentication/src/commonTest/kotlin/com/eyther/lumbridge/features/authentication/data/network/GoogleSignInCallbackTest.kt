package com.eyther.lumbridge.features.authentication.data.network

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.config.AuthenticationRedirect
import com.eyther.lumbridge.features.authentication.data.network.validation.parseGoogleSignInCallback
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GoogleSignInCallbackTest {

    @Test
    fun onlyTheConfiguredCallbackCanProvideAnAuthorizationCode() {
        val result = parseGoogleSignInCallback("${AuthenticationRedirect.URL}?code=authorization-code")
        assertEquals("authorization-code", assertIs<AppResult.Success<String>>(result).value)
        assertNull(parseGoogleSignInCallback("https://example.com/callback?code=other"))
        assertNull(parseGoogleSignInCallback("com.eyther.lumbridge://other/callback?code=other"))
        assertNull(parseGoogleSignInCallback("com.eyther.lumbridge://auth/other?code=other"))
    }

    @Test
    fun cancellationIsTypedAndProviderDescriptionsAreNotPropagated() {
        val result = parseGoogleSignInCallback(
            "${AuthenticationRedirect.URL}?error=access_denied&error_description=opaque-provider-text",
        )
        assertIs<AuthenticationError.GoogleSignInCancelled>(assertIs<AppResult.Failure>(result).error)
    }

    @Test
    fun missingOrAmbiguousCodesCannotStartATokenExchange() {
        val invalid = listOf(
            AuthenticationRedirect.URL,
            "${AuthenticationRedirect.URL}?code=",
            "${AuthenticationRedirect.URL}?code=first&code=second",
            "${AuthenticationRedirect.URL}#access_token=untrusted",
        )
        invalid.forEach { url ->
            assertIs<AuthenticationError.GoogleSignInFailed>(
                assertIs<AppResult.Failure>(parseGoogleSignInCallback(url)).error,
            )
        }
    }
}
