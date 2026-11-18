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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.eyther.lumbridge.app.navigation.model.sheet.TasksSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.settings.presentation.screen.NotificationsPrimeScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeScreenViewModelContract
import com.eyther.lumbridge.features.tasks.presentation.components.bottomsheet.TaskBottomSheet
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TasksSheetHost(
    viewModel: TasksScreenViewModelContract,
    timeZone: String,
    snackbarBottomPadding: Dp = 0.dp,
    primeOnOpen: Boolean = false,
    content: @Composable () -> Unit,
) {
    val sheetViewModelOwner = rememberHostViewModelStoreOwner()
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val notificationViewModel: NotificationsPrimeScreenViewModelContract =
        koinViewModel(viewModelStoreOwner = sheetViewModelOwner)
    val notificationState by notificationViewModel.state.platformCollectAsStateOnStart()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { notificationViewModel.onResume() }
    LaunchedEffect(primeOnOpen, state.loading, state.notificationPrimeRequest) {
        if (
            !state.showAdd &&
                (state.notificationPrimeRequest > 0 ||
                    (primeOnOpen &&
                        !state.loading &&
                        state.items.any { it.notificationEnabled && !it.completed }))
        )
            notificationViewModel.onContextRequested()
    }
    val requestedSheet =
        when {
            state.showAdd -> TasksSheets.Task
            notificationState.visible -> TasksSheets.Notifications
            else -> null
        }
    LaunchedEffect(requestedSheet, notificationState.busy, notificationState.error) {
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
    controller.registerBottomSheet<TasksSheets.Notifications> {
        NotificationsPrimeScreen(viewModel = notificationViewModel)
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy && !notificationState.busy,
        onDismissed = {
            when (requestedSheet) {
                TasksSheets.Task -> viewModel.onAddSheetVisibilityChanged(false)
                TasksSheets.Notifications -> notificationViewModel.onNotNow()
                null -> Unit
            }
        },
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
