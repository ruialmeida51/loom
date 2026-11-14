package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.verification_check_inbox
import com.eyther.lumbridge.features.authentication.generated.resources.verification_code_label
import com.eyther.lumbridge.features.authentication.generated.resources.verification_description
import com.eyther.lumbridge.features.authentication.generated.resources.verification_help
import com.eyther.lumbridge.features.authentication.generated.resources.verification_resend
import com.eyther.lumbridge.features.authentication.generated.resources.verification_resend_countdown
import com.eyther.lumbridge.features.authentication.generated.resources.verification_submit
import com.eyther.lumbridge.features.authentication.generated.resources.verify_email_title
import com.eyther.lumbridge.features.authentication.presentation.components.AuthenticationLayout
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmailVerificationScreen(viewModel: EmailVerificationScreenViewModelContract, onBack: () -> Unit) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerEmailVerificationScreen(
        email = state.email,
        code = state.code,
        verifying = state.verifying,
        resending = state.resending,
        canVerify = state.canVerify,
        canResend = state.canResend,
        resendInSeconds = state.resendInSeconds,
        error = state.error,
        message = state.message,
        onCodeChanged = viewModel::onCodeChanged,
        onVerify = viewModel::onVerify,
        onResend = viewModel::onResendCode,
        onDismissError = viewModel::onDismissError,
        onBack = onBack,
    )
}

@Composable
private fun InnerEmailVerificationScreen(
    email: String,
    code: String,
    verifying: Boolean,
    resending: Boolean,
    canVerify: Boolean,
    canResend: Boolean,
    resendInSeconds: Int,
    error: TextResource?,
    message: TextResource?,
    onCodeChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    AuthenticationLayout(title = stringResource(Res.string.verify_email_title), onBack = onBack) {
        PageHeading(
            eyebrow = stringResource(Res.string.verification_check_inbox),
            title = stringResource(Res.string.verify_email_title),
            subtitle = stringResource(Res.string.verification_description, email),
        )
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChanged,
            label = { Text(stringResource(Res.string.verification_code_label)) },
            modifier = Modifier.fillMaxWidth().testTag("verification_code").semantics { contentType = ContentType.SmsOtpCode },
            singleLine = true,
            enabled = !verifying && !resending,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onVerify() }),
        )
        error?.let { value -> ErrorNotice(message = value, onDismiss = onDismissError) }
        message?.let { value -> Text(value.getString(), color = MaterialTheme.colorScheme.secondary) }
        LoomButton(
            text = stringResource(Res.string.verification_submit),
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().testTag("verification_submit"),
            loading = verifying,
            enabled = canVerify,
        )
        Text(stringResource(Res.string.verification_help), style = MaterialTheme.typography.bodySmall)
        LoomButton(
            text = stringResource(Res.string.verification_resend),
            onClick = onResend,
            modifier = Modifier.fillMaxWidth().testTag("verification_resend"),
            loading = resending,
            enabled = canResend,
        )
        if (resendInSeconds > 0) {
            Text(
                text = stringResource(Res.string.verification_resend_countdown, resendInSeconds),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
