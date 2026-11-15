package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_add_expense
import com.eyther.lumbridge.features.home.generated.resources.home_add_reminder
import com.eyther.lumbridge.features.home.generated.resources.home_all_reminders
import com.eyther.lumbridge.features.home.generated.resources.home_feed
import com.eyther.lumbridge.features.home.generated.resources.home_manage_household
import com.eyther.lumbridge.features.home.generated.resources.home_nothing_due
import com.eyther.lumbridge.features.home.generated.resources.home_shopping
import com.eyther.lumbridge.features.home.generated.resources.home_today
import com.eyther.lumbridge.features.home.generated.resources.home_upcoming
import com.eyther.lumbridge.features.home.generated.resources.ui_try_again
import com.eyther.lumbridge.features.home.presentation.components.HomeReminderRow
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModelContract,
    context: HouseholdContextDomain,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onMoney: () -> Unit,
    onFeed: () -> Unit,
    onHousehold: () -> Unit = {},
    onAddReminder: () -> Unit = onReminders,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHomeScreen(
        loading = state.loading,
        hasOverview = state.hasOverview,
        reminders = state.reminders,
        hasMoreReminders = state.hasMoreReminders,
        upcomingReminders = state.upcomingReminders,
        error = state.error,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
        onShopping = onShopping,
        onReminders = onReminders,
        onAddExpense = onMoney,
        onFeed = onFeed,
        onHousehold = onHousehold,
        onAddReminder = onAddReminder,
    )
}

@Composable
private fun InnerHomeScreen(
    loading: Boolean,
    hasOverview: Boolean,
    reminders: List<HomeReminderUi>,
    hasMoreReminders: Boolean,
    upcomingReminders: List<HomeReminderUi>,
    error: TextResource?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onAddExpense: () -> Unit,
    onFeed: () -> Unit,
    onHousehold: () -> Unit,
    onAddReminder: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("home_content"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                title = stringResource(Res.string.home_today),
                action = stringResource(Res.string.home_add_reminder),
                onAction = onAddReminder,
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
                    val reminder = reminders[index]
                    HomeReminderRow(reminder = reminder, onClick = onReminders)
                }
            if (upcomingReminders.isNotEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.home_upcoming),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(count = upcomingReminders.size, key = { upcomingReminders[it].id }) { index ->
                    val reminder = upcomingReminders[index]
                    HomeReminderRow(reminder = reminder, onClick = onReminders)
                }
            }
        }
        if (hasMoreReminders)
            item {
                TextButton(onClick = onReminders) {
                    Text(stringResource(Res.string.home_all_reminders))
                }
            }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onShopping) { Text(stringResource(Res.string.home_shopping)) }
                LoomButton(
                    text = stringResource(Res.string.home_add_expense),
                    onClick = onAddExpense,
                    modifier = Modifier,
                )
            }
        }
        item {
            GroupedSurface {
                NavigationRow(title = stringResource(Res.string.home_feed), onClick = onFeed)
            }
        }
        item {
            TextButton(onClick = onHousehold) {
                Text(stringResource(Res.string.home_manage_household))
            }
        }
    }
}
