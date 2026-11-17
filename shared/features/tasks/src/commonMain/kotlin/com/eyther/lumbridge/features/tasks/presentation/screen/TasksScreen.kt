package com.eyther.lumbridge.features.tasks.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ActionFab
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.FloatingActionContentPadding
import com.eyther.lumbridge.core.presentation.components.IllustratedEmptyState
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_completed
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_title
import com.eyther.lumbridge.features.tasks.generated.resources.ui_add_a_task
import com.eyther.lumbridge.features.tasks.generated.resources.ui_nothing_on_your_mind
import com.eyther.lumbridge.features.tasks.presentation.components.TaskListRow
import com.eyther.lumbridge.features.tasks.presentation.model.TaskRowUi
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksScreen(viewModel: TasksScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        floatingActionButton = {
            ActionFab(
                text = stringResource(Res.string.ui_add_a_task),
                onClick = { viewModel.onAddSheetVisibilityChanged(true) },
                modifier = Modifier.testTag("add_task"),
            )
        },
    ) {
        InnerTasksScreen(
            openRows = state.openRows,
            completedRows = state.completedRows,
            showCompleted = state.showCompleted,
            completingIds = state.completingIds,
            removingIds = state.removingIds,
            loading = state.loading,
            showEmpty = state.showEmpty,
            error = state.displayError,
            onShowCompleted = viewModel::onCompletedVisibilityChanged,
            onDismiss = viewModel::onDismissError,
            onComplete = viewModel::onCompleteTask,
            onRemove = viewModel::onRemoveTask,
        )
    }
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.deleting,
            error = state.deleteError,
            confirmationTag = "confirm_delete_task",
            onConfirm = viewModel::onRemoveTaskConfirmed,
            onDismiss = viewModel::onRemoveTaskCancelled,
        )
    }
}

@Composable
private fun InnerTasksScreen(
    openRows: ImmutableList<TaskRowUi>,
    completedRows: ImmutableList<TaskRowUi>,
    showCompleted: Boolean,
    completingIds: ImmutableSet<String>,
    removingIds: ImmutableSet<String>,
    loading: Boolean,
    showEmpty: Boolean,
    error: TextResource?,
    onShowCompleted: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onComplete: (TaskDomain) -> Unit,
    onRemove: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = LoomSpacing.content)
                .testTag("tasks_content"),
        contentPadding =
            PaddingValues(top = LoomSpacing.medium, bottom = FloatingActionContentPadding),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
    ) {
        item(key = "header") {
            Text(
                stringResource(Res.string.tasks_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        error?.let { message ->
            item(key = "error") { ErrorNotice(message = message, onDismiss = onDismiss) }
        }
        if (loading) item(key = "loading") { LoadingRows(rows = 3) }
        if (showEmpty)
            item(key = "empty") {
                IllustratedEmptyState(
                    title = stringResource(Res.string.ui_nothing_on_your_mind),
                    glyph = Glyph.CHECK,
                )
            }
        if (!loading) {
            items(openRows, key = { it.task.id }, contentType = { "task" }) { row ->
                TaskListRow(
                    row = row,
                    completing = row.task.id in completingIds,
                    removing = row.task.id in removingIds,
                    onComplete = onComplete,
                    onRemove = onRemove,
                )
            }
            if (completedRows.isNotEmpty()) {
                item(key = "completed_header") {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SelectorButton(
                        text =
                            pluralStringResource(
                                Res.plurals.tasks_completed,
                                completedRows.size,
                                completedRows.size,
                            ),
                        expanded = showCompleted,
                        onClick = { onShowCompleted(!showCompleted) },
                        textStyle = MaterialTheme.typography.labelLarge,
                        modifier =
                            Modifier.padding(top = LoomSpacing.small).testTag("tasks_completed"),
                    )
                }
                if (showCompleted)
                    items(completedRows, key = { it.task.id }, contentType = { "task" }) { row ->
                        TaskListRow(
                            row = row,
                            completing = row.task.id in completingIds,
                            removing = row.task.id in removingIds,
                            onComplete = onComplete,
                            onRemove = onRemove,
                        )
                    }
            }
        }
    }
}
