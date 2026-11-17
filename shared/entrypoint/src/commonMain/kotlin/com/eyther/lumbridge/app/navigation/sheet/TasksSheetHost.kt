package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.navigation.model.sheet.TasksSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.tasks.presentation.components.bottomsheet.TaskBottomSheet
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract

@Composable
internal fun TasksSheetHost(
    viewModel: TasksScreenViewModelContract,
    timeZone: String,
    snackbarBottomPadding: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showAdd -> TasksSheets.Task
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<TasksSheets.Task> {
        TaskBottomSheet(
            title = state.title,
            date = state.date,
            time = state.time,
            hasDueDate = state.hasDueDate,
            notificationEnabled = state.notificationEnabled,
            recurrence = state.recurrence,
            busy = state.busy,
            formError = state.formError,
            timeZone = timeZone,
            onShowAdd = viewModel::onAddSheetVisibilityChanged,
            onTitle = viewModel::onTitleChanged,
            onDate = viewModel::onDateChanged,
            onTime = viewModel::onTimeChanged,
            onDueDateEnabled = viewModel::onDueDateEnabledChanged,
            onNotificationEnabled = viewModel::onNotificationEnabledChanged,
            onRecurrence = viewModel::onRecurrenceChanged,
            onDismiss = viewModel::onDismissError,
            onAdd = viewModel::onSaveTask,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = { viewModel.onAddSheetVisibilityChanged(false) },
        content = {
            Box(Modifier.fillMaxSize()) {
                content()
                SuccessSnackbar(
                    message = state.successMessage,
                    modifier =
                        Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = snackbarBottomPadding),
                )
            }
        },
    )
}
