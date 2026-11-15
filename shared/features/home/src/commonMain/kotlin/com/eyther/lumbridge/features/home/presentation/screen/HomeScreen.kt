package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_all_reminders
import com.eyther.lumbridge.features.home.generated.resources.home_nothing_due
import com.eyther.lumbridge.features.home.generated.resources.home_quick_actions
import com.eyther.lumbridge.features.home.generated.resources.home_today
import com.eyther.lumbridge.features.home.generated.resources.home_upcoming
import com.eyther.lumbridge.features.home.generated.resources.ui_try_again
import com.eyther.lumbridge.features.home.presentation.components.HomeQuickActions
import com.eyther.lumbridge.features.home.presentation.components.HomeReminderRow
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModelContract,
    onReminders: () -> Unit,
    onAddExpense: () -> Unit,
    onAddReminder: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHomeScreen(
        loading = state.loading,
        hasOverview = state.hasOverview,
        reminders = state.reminders,
        upcomingReminders = state.upcomingReminders,
        error = state.error,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
        onReminders = onReminders,
        onAddExpense = onAddExpense,
        onAddReminder = onAddReminder,
    )
}

@Composable
private fun InnerHomeScreen(
    loading: Boolean,
    hasOverview: Boolean,
    reminders: List<HomeReminderUi>,
    upcomingReminders: List<HomeReminderUi>,
    error: TextResource?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onReminders: () -> Unit,
    onAddExpense: () -> Unit,
    onAddReminder: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("home_content"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle(title = stringResource(Res.string.home_quick_actions)) }
        item { HomeQuickActions(onAddExpense = onAddExpense, onAddReminder = onAddReminder) }
        item {
            SectionTitle(
                title = stringResource(Res.string.home_today),
                action = stringResource(Res.string.home_all_reminders),
                onAction = onReminders,
            )
        }
        if (loading) item { LoadingRows() }
        error?.let { message ->
            item {
                ErrorNotice(message = message, onDismiss = onDismiss)
                TextButton(onClick = onRetry) { Text(stringResource(Res.string.ui_try_again)) }
            }
        }
        if (hasOverview && !loading) {
            if (reminders.isEmpty())
                item {
                    Text(
                        stringResource(Res.string.home_nothing_due),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("home_reminders_empty"),
                    )
                }
            else
                items(count = reminders.size, key = { reminders[it].id }) { index ->
                    HomeReminderRow(reminder = reminders[index], onClick = onReminders)
                }
            if (upcomingReminders.isNotEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.home_upcoming),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(count = upcomingReminders.size, key = { upcomingReminders[it].id }) { index ->
                    HomeReminderRow(reminder = upcomingReminders[index], onClick = onReminders)
                }
            }
        }
    }
}
