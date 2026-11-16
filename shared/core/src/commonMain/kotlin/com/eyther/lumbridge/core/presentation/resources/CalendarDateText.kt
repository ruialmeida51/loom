package com.eyther.lumbridge.core.presentation.resources

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.calendar_april
import com.eyther.lumbridge.core.generated.resources.calendar_august
import com.eyther.lumbridge.core.generated.resources.calendar_date
import com.eyther.lumbridge.core.generated.resources.calendar_date_without_year
import com.eyther.lumbridge.core.generated.resources.calendar_december
import com.eyther.lumbridge.core.generated.resources.calendar_february
import com.eyther.lumbridge.core.generated.resources.calendar_january
import com.eyther.lumbridge.core.generated.resources.calendar_july
import com.eyther.lumbridge.core.generated.resources.calendar_june
import com.eyther.lumbridge.core.generated.resources.calendar_march
import com.eyther.lumbridge.core.generated.resources.calendar_may
import com.eyther.lumbridge.core.generated.resources.calendar_november
import com.eyther.lumbridge.core.generated.resources.calendar_october
import com.eyther.lumbridge.core.generated.resources.calendar_september
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

/** A calendar label whose ordering and month names follow the selected resource language. */
@Composable
fun calendarDateText(date: LocalDate): String =
    calendarDateText(day = date.dayOfMonth, month = date.month, year = date.year)

@Composable
fun calendarDateText(day: Int, month: Month, year: Int? = null): String {
    val monthName =
        stringResource(
            when (month) {
                Month.JANUARY -> Res.string.calendar_january
                Month.FEBRUARY -> Res.string.calendar_february
                Month.MARCH -> Res.string.calendar_march
                Month.APRIL -> Res.string.calendar_april
                Month.MAY -> Res.string.calendar_may
                Month.JUNE -> Res.string.calendar_june
                Month.JULY -> Res.string.calendar_july
                Month.AUGUST -> Res.string.calendar_august
                Month.SEPTEMBER -> Res.string.calendar_september
                Month.OCTOBER -> Res.string.calendar_october
                Month.NOVEMBER -> Res.string.calendar_november
                Month.DECEMBER -> Res.string.calendar_december
            },
        )
    return if (year == null) stringResource(Res.string.calendar_date_without_year, day, monthName)
    else stringResource(Res.string.calendar_date, day, monthName, year)
}
