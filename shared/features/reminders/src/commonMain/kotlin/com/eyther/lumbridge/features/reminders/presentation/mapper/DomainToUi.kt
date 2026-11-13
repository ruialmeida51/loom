package com.eyther.lumbridge.features.reminders.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.token_daily
import com.eyther.lumbridge.features.reminders.generated.resources.token_no_repeat
import com.eyther.lumbridge.features.reminders.generated.resources.token_weekly
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun reminderRecurrenceLabel(value: String): String =
    when (value) {
        "none" -> stringResource(Res.string.token_no_repeat)
        "daily" -> stringResource(Res.string.token_daily)
        "weekly" -> stringResource(Res.string.token_weekly)
        else -> value
    }
