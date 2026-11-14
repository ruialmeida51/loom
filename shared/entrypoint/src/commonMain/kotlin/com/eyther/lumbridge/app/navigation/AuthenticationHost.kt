package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.navigation.model.AuthenticationDestination
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
import com.eyther.lumbridge.features.authentication.presentation.screen.EmailVerificationScreen
import com.eyther.lumbridge.features.authentication.presentation.screen.LoginScreen
import com.eyther.lumbridge.features.authentication.presentation.screen.RegisterScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun AuthenticationHost(onOpenLegalDocument: (LegalDocumentDomain) -> Unit) {
    val state = rememberNavigationState(
        startRoot = AuthenticationDestination.Login,
        roots = setOf(AuthenticationDestination.Login),
    ) {
        subclass(AuthenticationDestination.Login::class)
        subclass(AuthenticationDestination.Register::class)
        subclass(AuthenticationDestination.VerifyEmail::class)
    }
    val navigator = remember(state) { Navigator(state = state) }

    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { destination ->
        when (destination) {
            AuthenticationDestination.Login -> LoginScreen(
                viewModel = koinViewModel(),
                onRegister = { navigator.navigateTo(AuthenticationDestination.Register) },
                onVerifyEmail = { email, codeRequested, purpose ->
                    navigator.navigateTo(
                        AuthenticationDestination.VerifyEmail(
                            email = email,
                            codeRequested = codeRequested,
                            signInCode = purpose == EmailCodePurposeDomain.SIGN_IN,
                        ),
                    )
                },
                onOpenLegalDocument = onOpenLegalDocument,
            )
            AuthenticationDestination.Register -> RegisterScreen(
                viewModel = koinViewModel(),
                onVerifyEmail = { email ->
                    navigator.navigateTo(AuthenticationDestination.VerifyEmail(email = email, codeRequested = true))
                },
                onBack = navigator::goBack,
                onOpenLegalDocument = onOpenLegalDocument,
            )
            is AuthenticationDestination.VerifyEmail -> EmailVerificationScreen(
                viewModel = koinViewModel(parameters = {
                    parametersOf(
                        EmailVerificationScreenViewModelContract.Params(
                            email = destination.email,
                            codeRequested = destination.codeRequested,
                            purpose = if (destination.signInCode) {
                                EmailCodePurposeDomain.SIGN_IN
                            } else {
                                EmailCodePurposeDomain.REGISTRATION
                            },
                        ),
                    )
                }),
                onBack = navigator::goBack,
            )
        }
    }
}
