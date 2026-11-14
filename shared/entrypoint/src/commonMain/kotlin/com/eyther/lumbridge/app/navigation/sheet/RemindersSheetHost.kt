package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.RemindersSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.reminders.presentation.components.bottomsheet.ReminderBottomSheet
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract

@Composable
internal fun RemindersSheetHost(
    viewModel: RemindersScreenViewModelContract,
    timeZone: String,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showAdd -> RemindersSheets.Reminder
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<RemindersSheets.Reminder> {
        ReminderBottomSheet(
            title = state.title,
            date = state.date,
            time = state.time,
            recurrence = state.recurrence,
            busy = state.busy,
            formError = state.formError,
            timeZone = timeZone,
            onShowAdd = viewModel::onAddSheetVisibilityChanged,
            onTitle = viewModel::onTitleChanged,
            onDate = viewModel::onDateChanged,
            onTime = viewModel::onTimeChanged,
            onRecurrence = viewModel::onRecurrenceChanged,
            onDismiss = viewModel::onDismissError,
            onAdd = viewModel::onSaveReminder,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = { viewModel.onAddSheetVisibilityChanged(false) },
        content = content,
    )
}
