package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_summary_empty
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_summary_next
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_summary_paused
import com.eyther.lumbridge.features.expenses.generated.resources.ui_recurring_expenses
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringSummaryUi
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The way into recurring bills from the ledger, naming the next one due. */
@Composable
internal fun RecurringBillsRow(
    summary: RecurringSummaryUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description =
        when {
            summary.nextTitle != null && summary.nextOn != null ->
                pluralStringResource(
                    Res.plurals.recurring_summary_next,
                    summary.activeCount,
                    summary.activeCount,
                    summary.nextTitle,
                    calendarDateText(day = summary.nextOn.dayOfMonth, month = summary.nextOn.month),
                )
            summary.pausedCount > 0 ->
                pluralStringResource(
                    Res.plurals.recurring_summary_paused,
                    summary.pausedCount,
                    summary.pausedCount,
                )
            else -> stringResource(Res.string.recurring_summary_empty)
        }
    GroupedSurface(modifier = modifier) {
        NavigationRow(
            title = stringResource(Res.string.ui_recurring_expenses),
            description = description,
            value = summary.nextAmount,
            glyph = Glyph.REPEAT,
            onClick = onClick,
            modifier = Modifier.testTag("recurring_bills_row"),
        )
    }
}
