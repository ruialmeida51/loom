package com.eyther.lumbridge.features.reminders.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.date_absolute
import com.eyther.lumbridge.features.reminders.generated.resources.date_absolute_year
import com.eyther.lumbridge.features.reminders.generated.resources.date_april
import com.eyther.lumbridge.features.reminders.generated.resources.date_august
import com.eyther.lumbridge.features.reminders.generated.resources.date_days_ago
import com.eyther.lumbridge.features.reminders.generated.resources.date_december
import com.eyther.lumbridge.features.reminders.generated.resources.date_february
import com.eyther.lumbridge.features.reminders.generated.resources.date_in_days
import com.eyther.lumbridge.features.reminders.generated.resources.date_january
import com.eyther.lumbridge.features.reminders.generated.resources.date_july
import com.eyther.lumbridge.features.reminders.generated.resources.date_june
import com.eyther.lumbridge.features.reminders.generated.resources.date_label
import com.eyther.lumbridge.features.reminders.generated.resources.date_march
import com.eyther.lumbridge.features.reminders.generated.resources.date_may
import com.eyther.lumbridge.features.reminders.generated.resources.date_november
import com.eyther.lumbridge.features.reminders.generated.resources.date_october
import com.eyther.lumbridge.features.reminders.generated.resources.date_september
import com.eyther.lumbridge.features.reminders.generated.resources.date_today
import com.eyther.lumbridge.features.reminders.generated.resources.date_tomorrow
import com.eyther.lumbridge.features.reminders.generated.resources.date_yesterday
import com.eyther.lumbridge.features.reminders.presentation.model.ReminderDateUi
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderDueDate(date: ReminderDateUi, modifier: Modifier = Modifier) {
    val month =
        stringResource(
            when (date.month) {
                1 -> Res.string.date_january
                2 -> Res.string.date_february
                3 -> Res.string.date_march
                4 -> Res.string.date_april
                5 -> Res.string.date_may
                6 -> Res.string.date_june
                7 -> Res.string.date_july
                8 -> Res.string.date_august
                9 -> Res.string.date_september
                10 -> Res.string.date_october
                11 -> Res.string.date_november
                else -> Res.string.date_december
            },
        )
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
        if (date.year == null) stringResource(Res.string.date_absolute, date.day, month)
        else stringResource(Res.string.date_absolute_year, date.day, month, date.year)
    Text(
        text = stringResource(Res.string.date_label, relative, absolute, date.time),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color =
            if (date.daysFromToday < 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
