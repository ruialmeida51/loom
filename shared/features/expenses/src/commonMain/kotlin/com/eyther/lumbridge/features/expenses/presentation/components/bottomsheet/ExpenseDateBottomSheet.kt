package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_date
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDateBottomSheet(date: String, onSelected: (String) -> Unit, onClose: () -> Unit) {
    val picker =
        rememberDatePickerState(
            initialSelectedDateMillis =
                LocalDate.parse(date).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        )
    FormBottomSheet(
        title = stringResource(Res.string.form_date),
        onAction = {
            picker.selectedDateMillis?.let {
                onSelected(
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString(),
                )
            }
        },
        onDismiss = onClose,
        actionEnabled = picker.selectedDateMillis != null,
        actionModifier = Modifier.testTag("confirm_expense_date"),
    ) {
        DatePicker(state = picker, showModeToggle = false)
    }
}
