package com.eyther.lumbridge.features.reminders.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.reminders.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("reminders.message_give_your_reminder_a_title_up_to_160_characters") ->
            stringResource(Res.string.message_give_your_reminder_a_title_up_to_160_characters)
        code.contains("reminders.message_choose_a_repeat_option") ->
            stringResource(Res.string.message_choose_a_repeat_option)
        code.contains("reminders.message_use_a_date_like_2026_11_13_and_a") ->
            stringResource(Res.string.message_use_a_date_like_2026_11_13_and_a)
        else -> genericErrorMessage()
    }

@Composable
internal fun localizedToken(value: String): String =
    when (value) {
        "none" -> stringResource(Res.string.token_no_repeat)
        "daily" -> stringResource(Res.string.token_daily)
        "weekly" -> stringResource(Res.string.token_weekly)
        else -> value
    }
