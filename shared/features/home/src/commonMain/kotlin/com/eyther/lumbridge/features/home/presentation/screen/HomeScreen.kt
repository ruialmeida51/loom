package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_all_reminders
import com.eyther.lumbridge.features.home.generated.resources.home_nothing_due
import com.eyther.lumbridge.features.home.generated.resources.home_quick_actions
import com.eyther.lumbridge.features.home.generated.resources.home_reminders
import com.eyther.lumbridge.features.home.generated.resources.home_shopping
import com.eyther.lumbridge.features.home.generated.resources.home_shopping_view_all
import com.eyther.lumbridge.features.home.generated.resources.ui_try_again
import com.eyther.lumbridge.features.home.presentation.components.HomeQuickActions
import com.eyther.lumbridge.features.home.presentation.components.HomeReminderRow
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingPreview
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModelContract,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onAddExpense: () -> Unit,
    onAddReminder: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerHomeScreen(
            loading = state.loading || state.refreshing,
            hasOverview = state.hasOverview,
            reminders = state.reminders,
            shoppingItems = state.shoppingItems,
            shoppingError = state.shoppingError,
            error = state.error,
            onRetry = viewModel::onRetry,
            onDismiss = viewModel::onDismissError,
            onReminders = onReminders,
            onShopping = onShopping,
            onAddExpense = onAddExpense,
            onAddReminder = onAddReminder,
        )
    }
}

@Composable
private fun InnerHomeScreen(
    loading: Boolean,
    hasOverview: Boolean,
    reminders: ImmutableList<HomeReminderUi>,
    shoppingItems: ImmutableList<ShoppingItemDomain>,
    shoppingError: TextResource?,
    error: TextResource?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onShopping: () -> Unit,
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
                title = stringResource(Res.string.home_reminders),
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
            if (shoppingItems.isNotEmpty()) {
                item(key = "shopping_preview") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("home_shopping_preview"),
                    ) {
                        SectionTitle(
                            title = stringResource(Res.string.home_shopping),
                            action = stringResource(Res.string.home_shopping_view_all),
                            onAction = onShopping,
                        )
                        ShoppingPreview(items = shoppingItems, onOpen = onShopping)
                    }
                }
            }
            shoppingError?.let { message ->
                item { ErrorNotice(message = message, onDismiss = onDismiss) }
            }
        }
    }
}
