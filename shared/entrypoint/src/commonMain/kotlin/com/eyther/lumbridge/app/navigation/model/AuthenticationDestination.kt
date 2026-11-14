package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Serializable
@Stable
internal sealed interface AuthenticationDestination : NavDestination {
    @Serializable
    @Stable
    data object Login : AuthenticationDestination

    @Serializable
    @Stable
    data object Register : AuthenticationDestination

    @Serializable
    @Stable
    data class VerifyEmail(
        val email: String,
        val codeRequested: Boolean,
        val signInCode: Boolean = false,
    ) : AuthenticationDestination
}
