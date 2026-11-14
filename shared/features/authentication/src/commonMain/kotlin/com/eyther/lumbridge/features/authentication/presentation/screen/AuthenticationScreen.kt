package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.accept_terms
import com.eyther.lumbridge.features.authentication.generated.resources.ui_a_calmer_home_for_the_things_you_share
import com.eyther.lumbridge.features.authentication.generated.resources.ui_already_have_an_account_sign_in
import com.eyther.lumbridge.features.authentication.generated.resources.ui_create_account
import com.eyther.lumbridge.features.authentication.generated.resources.ui_email
import com.eyther.lumbridge.features.authentication.generated.resources.ui_life_woven_together
import com.eyther.lumbridge.features.authentication.generated.resources.ui_make_yourself_at_home
import com.eyther.lumbridge.features.authentication.generated.resources.ui_new_here_create_an_account
import com.eyther.lumbridge.features.authentication.generated.resources.ui_password
import com.eyther.lumbridge.features.authentication.generated.resources.ui_sign_in
import com.eyther.lumbridge.features.authentication.generated.resources.ui_welcome_home
import com.eyther.lumbridge.features.authentication.generated.resources.ui_welcome_to_loom
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreen(
    viewModel: AuthenticationScreenViewModelContract,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerAuthenticationScreen(
        email = state.email,
        acceptedTerms = state.acceptedTerms,
        onTermsAccepted = viewModel::onTermsAcceptedChanged,
        onOpenLegalDocument = onOpenLegalDocument,
        password = state.password,
        registering = state.registering,
        busy = state.busy,
        message = state.message,
        error = state.error,
        onEmail = viewModel::onEmailChanged,
        onPassword = viewModel::onPasswordChanged,
        onToggleRegister = viewModel::onToggleRegistration,
        onSubmit = viewModel::onSubmit,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerAuthenticationScreen(
    email: String,
    acceptedTerms: Boolean,
    onTermsAccepted: (Boolean) -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
    password: String,
    registering: Boolean,
    busy: Boolean,
    message: TextResource?,
    error: TextResource?,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onToggleRegister: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        PageHeading(
            stringResource(Res.string.ui_welcome_to_loom),
            stringResource(Res.string.ui_life_woven_together),
            stringResource(Res.string.ui_a_calmer_home_for_the_things_you_share),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (registering) stringResource(Res.string.ui_make_yourself_at_home)
            else stringResource(Res.string.ui_welcome_home),
            style = MaterialTheme.typography.headlineMedium,
        )
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
        message?.let { Text(it.getString(), color = MaterialTheme.colorScheme.secondary) }
        OutlinedTextField(
            email,
            onEmail,
            label = { Text(stringResource(Res.string.ui_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            password,
            onPassword,
            label = { Text(stringResource(Res.string.ui_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (registering) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = onTermsAccepted,
                    enabled = !busy,
                )
                Text(stringResource(Res.string.accept_terms))
            }
        }
        LegalLinks(onOpenDocument = onOpenLegalDocument, showAccountDeletion = true)
        LoomButton(
            if (registering) stringResource(Res.string.ui_create_account)
            else stringResource(Res.string.ui_sign_in),
            onSubmit,
            Modifier.fillMaxWidth(),
            loading = busy,
        )
        TextButton(onClick = onToggleRegister) {
            Text(
                if (registering) stringResource(Res.string.ui_already_have_an_account_sign_in)
                else stringResource(Res.string.ui_new_here_create_an_account),
            )
        }
    }
}
