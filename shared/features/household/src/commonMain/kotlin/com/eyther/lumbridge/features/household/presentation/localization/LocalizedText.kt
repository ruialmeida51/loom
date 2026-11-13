package com.eyther.lumbridge.features.household.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.household.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("household.message_enter_the_invitation_code_you_received") ->
            stringResource(Res.string.message_enter_the_invitation_code_you_received)
        code.contains("household.message_give_your_household_a_name_between_2_and_60") ->
            stringResource(Res.string.message_give_your_household_a_name_between_2_and_60)
        code.contains("household.message_choose_a_supported_currency") ->
            stringResource(Res.string.message_choose_a_supported_currency)
        else -> genericErrorMessage()
    }
