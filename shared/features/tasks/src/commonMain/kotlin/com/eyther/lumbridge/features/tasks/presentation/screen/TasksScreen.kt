package com.eyther.lumbridge.features.tasks.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ActionFab
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.FloatingActionContentPadding
import com.eyther.lumbridge.core.presentation.components.IllustratedEmptyState
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.SegmentedTab
import com.eyther.lumbridge.core.presentation.components.SegmentedTabs
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.layout.RootHeading
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_filter_all
import com.eyther.lumbridge.features.tasks.generated.resources.task_filter_mine
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_completed
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_group_anytime
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_group_later
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_group_overdue
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_group_today
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_group_tomorrow
import com.eyther.lumbridge.features.tasks.generated.resources.tasks_title
import com.eyther.lumbridge.features.tasks.generated.resources.ui_add_a_task
import com.eyther.lumbridge.features.tasks.generated.resources.ui_nothing_on_your_mind
import com.eyther.lumbridge.features.tasks.presentation.components.TaskListRow
import com.eyther.lumbridge.features.tasks.presentation.model.TaskFilterUi
import com.eyther.lumbridge.features.tasks.presentation.model.TaskGroupKindUi
import com.eyther.lumbridge.features.tasks.presentation.model.TaskGroupUi
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
            openGroups = state.openGroups,
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
            onAssign = viewModel::onAssignRequested,
            showsPeople = state.showsPeople,
            userId = state.userId,
            filter = state.filter,
            onFilter = viewModel::onFilterChanged,
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
    openGroups: ImmutableList<TaskGroupUi>,
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
    onAssign: (TaskDomain) -> Unit = {},
    showsPeople: Boolean = false,
    userId: String = "",
    filter: TaskFilterUi = TaskFilterUi.ALL,
    onFilter: (TaskFilterUi) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("tasks_content"),
        contentPadding = rootContentPadding(bottom = FloatingActionContentPadding),
        verticalArrangement = Arrangement.spacedBy(TaskRowSpacing),
    ) {
        item(key = "header") {
            RootHeading(
                title = stringResource(Res.string.tasks_title),
                modifier = Modifier.padding(bottom = LoomSpacing.content - LoomSpacing.extraSmall),
            )
        }
        if (showsPeople)
            item(key = "filter") {
                SegmentedTabs(
                    tabs =
                        listOf(
                            SegmentedTab(
                                label = stringResource(Res.string.task_filter_all),
                                tag = "tasks_filter_all",
                            ),
                            SegmentedTab(
                                label = stringResource(Res.string.task_filter_mine),
                                tag = "tasks_filter_mine",
                            ),
                        ),
                    selectedIndex = filter.ordinal,
                    onSelect = { index -> onFilter(TaskFilterUi.entries[index]) },
                    modifier = Modifier.padding(bottom = LoomSpacing.small),
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
            openGroups.forEach { group ->
                item(key = "group_${group.kind}", contentType = "group_header") {
                    TaskGroupHeader(kind = group.kind, count = group.rows.size)
                }
                itemsIndexed(group.rows, key = { _, row -> row.task.id }, contentType = { _, _ -> "task" }) {
                    index,
                    row ->
                    TaskListRow(
                        row = row,
                        completing = row.task.id in completingIds,
                        removing = row.task.id in removingIds,
                        onComplete = onComplete,
                        onRemove = onRemove,
                        onAssign = onAssign,
                        showsPeople = showsPeople,
                        userId = userId,
                        first = index == 0,
                        last = index == group.rows.lastIndex,
                        gap = TaskRowSpacing,
                    )
                }
            }
            if (completedRows.isNotEmpty()) {
                item(key = "completed_header") {
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
                    itemsIndexed(completedRows, key = { _, row -> row.task.id }, contentType = { _, _ -> "task" }) {
                        index,
                        row ->
                        TaskListRow(
                            row = row,
                            completing = row.task.id in completingIds,
                            removing = row.task.id in removingIds,
                            onComplete = onComplete,
                            onRemove = onRemove,
                            onAssign = onAssign,
                            showsPeople = showsPeople,
                            userId = userId,
                            first = index == 0,
                            last = index == completedRows.lastIndex,
                            gap = TaskRowSpacing,
                        )
                    }
            }
        }
    }
}

private val TaskRowSpacing = LoomSpacing.extraSmall

@Composable
private fun TaskGroupHeader(kind: TaskGroupKindUi, count: Int) {
    Text(
        text =
            "${stringResource(
                when (kind) {
                    TaskGroupKindUi.OVERDUE -> Res.string.tasks_group_overdue
                    TaskGroupKindUi.TODAY -> Res.string.tasks_group_today
                    TaskGroupKindUi.TOMORROW -> Res.string.tasks_group_tomorrow
                    TaskGroupKindUi.LATER -> Res.string.tasks_group_later
                    TaskGroupKindUi.ANYTIME -> Res.string.tasks_group_anytime
                },
            )} · $count",
        style = MaterialTheme.typography.titleSmall,
        color =
            if (kind == TaskGroupKindUi.OVERDUE) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(top = LoomSpacing.small, bottom = 2.dp, start = 4.dp)
                .testTag("task_group_${kind.name.lowercase()}"),
    )
}
