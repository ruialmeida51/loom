package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.login_description
import com.eyther.lumbridge.features.authentication.generated.resources.login_email_code
import com.eyther.lumbridge.features.authentication.generated.resources.ui_new_here_create_an_account
import com.eyther.lumbridge.features.authentication.generated.resources.ui_sign_in
import com.eyther.lumbridge.features.authentication.generated.resources.ui_welcome_home
import com.eyther.lumbridge.features.authentication.generated.resources.ui_welcome_to_loom
import com.eyther.lumbridge.features.authentication.presentation.components.AuthenticationLayout
import com.eyther.lumbridge.features.authentication.presentation.components.EmailInput
import com.eyther.lumbridge.features.authentication.presentation.components.PasswordInput
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract.RedirectTo
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModelContract,
    onRegister: () -> Unit,
    onVerifyEmail: (String, Boolean, EmailCodePurposeDomain) -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    HandleRedirects(
        redirectTo = state.redirectTo,
        onRegister = onRegister,
        onVerifyEmail = onVerifyEmail,
        onHandled = viewModel::onRedirectHandled,
    )
    InnerLoginScreen(
        email = state.email,
        password = state.password,
        busy = state.busy,
        signingIn = state.signingIn,
        requestingCode = state.requestingCode,
        canRequestCode = state.canRequestCode,
        canSubmit = state.canSubmit,
        error = state.error,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSubmit = viewModel::onSubmit,
        onRegister = viewModel::onCreateAccount,
        onRequestCode = viewModel::onRequestEmailCode,
        onDismissError = viewModel::onDismissError,
        onOpenLegalDocument = onOpenLegalDocument,
    )
}

@Composable
private fun InnerLoginScreen(
    email: String,
    password: String,
    busy: Boolean,
    signingIn: Boolean,
    requestingCode: Boolean,
    canRequestCode: Boolean,
    canSubmit: Boolean,
    error: TextResource?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegister: () -> Unit,
    onRequestCode: () -> Unit,
    onDismissError: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    AuthenticationLayout(title = stringResource(Res.string.ui_sign_in)) {
        PageHeading(
            eyebrow = stringResource(Res.string.ui_welcome_to_loom),
            title = stringResource(Res.string.ui_welcome_home),
            subtitle = stringResource(Res.string.login_description),
        )
        EmailInput(value = email, onValueChanged = onEmailChanged, enabled = !busy)
        PasswordInput(value = password, onValueChanged = onPasswordChanged, enabled = !busy, onSubmit = onSubmit)
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismissError)
        }
        LoomButton(
            text = stringResource(Res.string.ui_sign_in),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().testTag("login_submit"),
            loading = signingIn,
            enabled = canSubmit && !requestingCode,
        )
        LoomButton(
            text = stringResource(Res.string.login_email_code),
            onClick = onRequestCode,
            modifier = Modifier.fillMaxWidth().testTag("login_email_code"),
            loading = requestingCode,
            enabled = canRequestCode && !signingIn,
        )
        TextButton(onClick = onRegister, enabled = !busy) {
            Text(stringResource(Res.string.ui_new_here_create_an_account))
        }
        LegalLinks(onOpenDocument = onOpenLegalDocument, showAccountDeletion = true)
    }
}

@Composable
private fun HandleRedirects(
    redirectTo: RedirectTo?,
    onRegister: () -> Unit,
    onVerifyEmail: (String, Boolean, EmailCodePurposeDomain) -> Unit,
    onHandled: () -> Unit,
) {
    LaunchedEffect(redirectTo) {
        val destination = redirectTo ?: return@LaunchedEffect
        onHandled()
        when (destination) {
            RedirectTo.Register -> onRegister()
            is RedirectTo.VerifyEmail -> onVerifyEmail(destination.email, destination.codeRequested, destination.purpose)
        }
    }
}
