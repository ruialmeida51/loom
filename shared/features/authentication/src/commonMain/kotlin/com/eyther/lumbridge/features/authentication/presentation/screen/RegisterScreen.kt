package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.accept_terms
import com.eyther.lumbridge.features.authentication.generated.resources.register_description
import com.eyther.lumbridge.features.authentication.generated.resources.ui_already_have_an_account_sign_in
import com.eyther.lumbridge.features.authentication.generated.resources.ui_create_account
import com.eyther.lumbridge.features.authentication.generated.resources.ui_make_yourself_at_home
import com.eyther.lumbridge.features.authentication.presentation.components.AuthenticationLayout
import com.eyther.lumbridge.features.authentication.presentation.components.EmailInput
import com.eyther.lumbridge.features.authentication.presentation.components.PasswordInput
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract.RedirectTo
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModelContract,
    onVerifyEmail: (String) -> Unit,
    onBack: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    HandleRedirects(
        redirectTo = state.redirectTo,
        onVerifyEmail = onVerifyEmail,
        onHandled = viewModel::onRedirectHandled,
    )
    InnerRegisterScreen(
        email = state.email,
        password = state.password,
        acceptedTerms = state.acceptedTerms,
        busy = state.busy,
        canSubmit = state.canSubmit,
        error = state.error,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTermsAccepted = viewModel::onTermsAcceptedChanged,
        onSubmit = viewModel::onSubmit,
        onDismissError = viewModel::onDismissError,
        onBack = onBack,
        onOpenLegalDocument = onOpenLegalDocument,
    )
}

@Composable
private fun InnerRegisterScreen(
    email: String,
    password: String,
    acceptedTerms: Boolean,
    busy: Boolean,
    canSubmit: Boolean,
    error: TextResource?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTermsAccepted: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    AuthenticationLayout(title = stringResource(Res.string.ui_create_account), onBack = onBack) {
        PageHeading(
            title = stringResource(Res.string.ui_make_yourself_at_home),
            subtitle = stringResource(Res.string.register_description),
        )
        EmailInput(value = email, onValueChanged = onEmailChanged, enabled = !busy)
        PasswordInput(
            value = password,
            onValueChanged = onPasswordChanged,
            enabled = !busy,
            onSubmit = onSubmit,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = onTermsAccepted,
                enabled = !busy,
                modifier = Modifier.testTag("accept_terms"),
            )
            Text(stringResource(Res.string.accept_terms))
        }
        LegalLinks(onOpenDocument = onOpenLegalDocument, showAccountDeletion = true)
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismissError)
        }
        LoomButton(
            text = stringResource(Res.string.ui_create_account),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().testTag("register_submit"),
            loading = busy,
            enabled = canSubmit,
        )
        TextButton(onClick = onBack, enabled = !busy) {
            Text(stringResource(Res.string.ui_already_have_an_account_sign_in))
        }
    }
}

@Composable
private fun HandleRedirects(
    redirectTo: RedirectTo?,
    onVerifyEmail: (String) -> Unit,
    onHandled: () -> Unit,
) {
    LaunchedEffect(redirectTo) {
        val destination = redirectTo ?: return@LaunchedEffect
        onHandled()
        when (destination) {
            is RedirectTo.VerifyEmail -> onVerifyEmail(destination.email)
        }
    }
}
