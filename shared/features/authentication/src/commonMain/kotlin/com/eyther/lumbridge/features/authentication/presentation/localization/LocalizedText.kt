package com.eyther.lumbridge.features.authentication.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.authentication.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("authentication.message_use_a_valid_email_and_a_password_with_at") ->
            stringResource(Res.string.message_use_a_valid_email_and_a_password_with_at)
        code.contains("authentication.message_enter_your_email_and_password") ->
            stringResource(Res.string.message_enter_your_email_and_password)
        code.contains(
            "authentication.message_check_your_email_to_confirm_your_account_then_sign",
        ) -> stringResource(Res.string.message_check_your_email_to_confirm_your_account_then_sign)
        else -> genericErrorMessage()
    }
