package com.eyther.lumbridge.features.reminders.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.date_days_ago
import com.eyther.lumbridge.features.reminders.generated.resources.date_in_days
import com.eyther.lumbridge.features.reminders.generated.resources.date_label
import com.eyther.lumbridge.features.reminders.generated.resources.date_today
import com.eyther.lumbridge.features.reminders.generated.resources.date_tomorrow
import com.eyther.lumbridge.features.reminders.generated.resources.date_yesterday
import com.eyther.lumbridge.features.reminders.presentation.model.ReminderDateUi
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderDueDate(date: ReminderDateUi, modifier: Modifier = Modifier) {
    val relative =
        when (date.daysFromToday) {
            0 -> stringResource(Res.string.date_today)
            1 -> stringResource(Res.string.date_tomorrow)
            -1 -> stringResource(Res.string.date_yesterday)
            else ->
                if (date.daysFromToday > 0)
                    pluralStringResource(
                        Res.plurals.date_in_days,
                        date.daysFromToday,
                        date.daysFromToday,
                    )
                else
                    pluralStringResource(
                        Res.plurals.date_days_ago,
                        -date.daysFromToday,
                        -date.daysFromToday,
                    )
        }
    val absolute =
        calendarDateText(day = date.day, month = Month.entries[date.month - 1], year = date.year)
    Text(
        text = stringResource(Res.string.date_label, relative, absolute, date.time),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color =
            if (date.daysFromToday < 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
