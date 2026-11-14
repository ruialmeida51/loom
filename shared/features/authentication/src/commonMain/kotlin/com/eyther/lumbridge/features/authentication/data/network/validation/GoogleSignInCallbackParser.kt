package com.eyther.lumbridge.features.authentication.data.network.validation

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.config.AuthenticationRedirect
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException

/** Ignores unrelated links and accepts only a code for the configured PKCE callback. */
internal fun parseGoogleSignInCallback(value: String): AppResult<String>? {
    val url = try {
        Url(value)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        return null
    }
    if (url.protocol.name != AuthenticationRedirect.SCHEME ||
        url.host != AuthenticationRedirect.HOST ||
        url.encodedPath != AuthenticationRedirect.PATH
    ) {
        return null
    }

    val error = url.parameters["error"]
    if (error != null) {
        return AppResult.Failure(
            error = if (error == "access_denied") {
                AuthenticationError.GoogleSignInCancelled()
            } else {
                AuthenticationError.GoogleSignInFailed()
            },
        )
    }
    val code = url.parameters.getAll("code")?.singleOrNull()?.takeIf { it.isNotBlank() }
        ?: return AppResult.Failure(AuthenticationError.GoogleSignInFailed())
    return AppResult.Success(code)
}
