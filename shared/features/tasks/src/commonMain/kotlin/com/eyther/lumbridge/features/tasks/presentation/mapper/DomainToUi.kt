package com.eyther.lumbridge.features.tasks.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.token_daily
import com.eyther.lumbridge.features.tasks.generated.resources.token_no_repeat
import com.eyther.lumbridge.features.tasks.generated.resources.token_weekly
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun taskRecurrenceLabel(value: TaskRecurrenceDomain): String =
    when (value) {
        TaskRecurrenceDomain.NONE -> stringResource(Res.string.token_no_repeat)
        TaskRecurrenceDomain.DAILY -> stringResource(Res.string.token_daily)
        TaskRecurrenceDomain.WEEKLY -> stringResource(Res.string.token_weekly)
    }
