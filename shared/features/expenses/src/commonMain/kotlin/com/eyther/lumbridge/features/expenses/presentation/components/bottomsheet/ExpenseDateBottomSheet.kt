package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_date
import com.eyther.lumbridge.features.expenses.generated.resources.split_done
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
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
        action = {
            TextButton(
                enabled = picker.selectedDateMillis != null,
                onClick = {
                    picker.selectedDateMillis?.let {
                        onSelected(
                            Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.UTC)
                                .date
                                .toString(),
                        )
                    }
                },
            ) {
                Text(stringResource(Res.string.split_done))
            }
        },
        dismissAction = {
            TextButton(onClick = onClose) { Text(stringResource(Res.string.ui_cancel)) }
        },
    ) {
        DatePicker(state = picker, showModeToggle = false)
    }
}
