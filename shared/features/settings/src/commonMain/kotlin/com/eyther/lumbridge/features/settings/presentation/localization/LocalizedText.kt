package com.eyther.lumbridge.features.settings.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.settings.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("settings.message_choose_a_theme") ->
            stringResource(Res.string.message_choose_a_theme)
        code.contains("settings.message_push_notifications_are_available_in_the_android_and_ios") ->
            stringResource(
                Res.string.message_push_notifications_are_available_in_the_android_and_ios,
            )
        code.contains("settings.message_notifications_aren_t_available_in_this_build_yet") ->
            stringResource(Res.string.message_notifications_aren_t_available_in_this_build_yet)
        code.contains("settings.message_allow_notifications_in_your_phone_s_settings_to_receive") ->
            stringResource(
                Res.string.message_allow_notifications_in_your_phone_s_settings_to_receive,
            )
        code.contains("settings.message_couldn_t_register_notifications_please_try_again") ->
            stringResource(Res.string.message_couldn_t_register_notifications_please_try_again)
        else -> genericErrorMessage()
    }

@Composable
internal fun localizedToken(value: String): String =
    when (value) {
        "SYSTEM" -> stringResource(Res.string.token_system)
        "LIGHT" -> stringResource(Res.string.token_light)
        "DARK" -> stringResource(Res.string.token_dark)
        "owner" -> stringResource(Res.string.token_owner)
        "member" -> stringResource(Res.string.token_member)
        else -> value
    }
