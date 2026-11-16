package com.eyther.lumbridge.features.reminders.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveIconButton
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.ListRow
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.reminder_delete_description
import com.eyther.lumbridge.features.reminders.generated.resources.ui_add_a_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_add_a_reminder_and_let_loom_keep_track_of
import com.eyther.lumbridge.features.reminders.generated.resources.ui_nothing_on_your_mind
import com.eyther.lumbridge.features.reminders.generated.resources.ui_repeats_1_s
import com.eyther.lumbridge.features.reminders.generated.resources.ui_room_in_your_head
import com.eyther.lumbridge.features.reminders.presentation.components.ReminderDueDate
import com.eyther.lumbridge.features.reminders.presentation.mapper.reminderRecurrenceLabel
import com.eyther.lumbridge.features.reminders.presentation.model.ReminderRowUi
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
fun RemindersScreen(viewModel: RemindersScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        snackbarHost = { SuccessSnackbar(message = state.successMessage) },
    ) {
        InnerRemindersScreen(
            items = state.rows,
            completingIds = state.completingIds,
            removingIds = state.removingIds,
            loading = state.loading || state.refreshing,
            showEmpty = state.showEmpty,
            error = state.displayError,
            onShowAdd = viewModel::onAddSheetVisibilityChanged,
            onDismiss = viewModel::onDismissError,
            onComplete = viewModel::onCompleteReminder,
            onRemove = viewModel::onRemoveReminder,
        )
    }
}

@Composable
private fun InnerRemindersScreen(
    items: ImmutableList<ReminderRowUi>,
    completingIds: ImmutableSet<String>,
    removingIds: ImmutableSet<String>,
    loading: Boolean,
    showEmpty: Boolean,
    error: TextResource?,
    onShowAdd: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onComplete: (ReminderDomain) -> Unit,
    onRemove: (String) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("reminders_content"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                stringResource(Res.string.ui_room_in_your_head),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
            }
        }
        item {
            LoomButton(
                stringResource(Res.string.ui_add_a_reminder),
                { onShowAdd(true) },
                Modifier.fillMaxWidth(),
            )
        }
        if (loading) item { LoadingRows() }
        if (showEmpty)
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_nothing_on_your_mind),
                    stringResource(Res.string.ui_add_a_reminder_and_let_loom_keep_track_of),
                )
            }
        if (!loading)
            items(items, key = { it.reminder.id }) { row ->
                val reminder = row.reminder
                val completing = reminder.id in completingIds
                val removing = reminder.id in removingIds
                ListRow {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (completing)
                            CircularProgressIndicator(
                                Modifier.size(48.dp).padding(12.dp),
                                strokeWidth = 2.dp,
                            )
                        else
                            Checkbox(
                                reminder.completed,
                                { onComplete(reminder) },
                                enabled = !removing,
                                modifier =
                                    Modifier.testTag("complete_${reminder.id}").semantics {
                                        contentDescription = reminder.title
                                    },
                            )
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                            ReminderDueDate(date = row.date)
                            if (reminder.recurrence != "none")
                                Text(
                                    stringResource(
                                        Res.string.ui_repeats_1_s,
                                        reminderRecurrenceLabel(reminder.recurrence),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                        }
                        DestructiveIconButton(
                            description =
                                stringResource(
                                    Res.string.reminder_delete_description,
                                    reminder.title,
                                ),
                            onClick = { onRemove(reminder.id) },
                            enabled = !completing,
                            loading = removing,
                        )
                    }
                }
            }
    }
}
