package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseStatusDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.history_edit
import com.eyther.lumbridge.features.expenses.generated.resources.ui_1_s_monthly
import com.eyther.lumbridge.features.expenses.generated.resources.ui_active
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancelled
import com.eyther.lumbridge.features.expenses.generated.resources.ui_each_active_schedule_records_an_expense_on_its_due
import com.eyther.lumbridge.features.expenses.generated.resources.ui_former_member
import com.eyther.lumbridge.features.expenses.generated.resources.ui_monthly_day
import com.eyther.lumbridge.features.expenses.generated.resources.ui_next_entry_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_no_recurring_expenses_yet
import com.eyther.lumbridge.features.expenses.generated.resources.ui_pause
import com.eyther.lumbridge.features.expenses.generated.resources.ui_paused
import com.eyther.lumbridge.features.expenses.generated.resources.ui_payer_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.expenses.generated.resources.ui_rent_internet_and_subscriptions_can_live_here
import com.eyther.lumbridge.features.expenses.generated.resources.ui_resume
import com.eyther.lumbridge.features.expenses.generated.resources.ui_resuming_schedules_the_next_future_entry_paused_months_are
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringExpenseUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecurringExpensesScreen(
    viewModel: RecurringExpensesScreenViewModelContract,
    onEdit: (RecurringExpenseDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerRecurringExpensesScreen(
            rules = state.rows,
            showEmpty = state.showEmpty,
            loading = state.loading,
            busy = state.busy,
            error = state.displayError,
            onEdit = onEdit,
            onDeleteRequested = viewModel::onDeleteRequested,
            onControl = viewModel::onControlSchedule,
            onRetry = viewModel::onRetry,
            onDismiss = viewModel::onDismissError,
        )
    }
    state.deletionConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            confirmationTag = "confirm_delete_recurring",
            error = state.error,
            onConfirm = viewModel::onDeleteConfirmed,
            onDismiss = viewModel::onDeleteDismissed,
        )
    }
}

@Composable
private fun InnerRecurringExpensesScreen(
    rules: ImmutableList<RecurringExpenseUi>,
    showEmpty: Boolean,
    loading: Boolean,
    busy: Boolean,
    error: TextResource?,
    onEdit: (RecurringExpenseDomain) -> Unit,
    onDeleteRequested: (RecurringExpenseDomain) -> Unit,
    onControl: (RecurringExpenseDomain, RecurringExpenseActionDomain) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().testTag("recurring_content"),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        contentPadding = rootContentPadding(),
    ) {
        item {
            Text(
                stringResource(Res.string.ui_each_active_schedule_records_an_expense_on_its_due),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(Res.string.ui_refresh_and_retry))
                }
            }
        }
        if (loading) item { LoadingRows() }
        if (showEmpty && !loading)
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_no_recurring_expenses_yet),
                    stringResource(Res.string.ui_rent_internet_and_subscriptions_can_live_here),
                )
            }
        if (!loading)
            items(rules, key = { it.rule.id }) { row ->
                val rule = row.rule
                LoomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
                        ) {
                            Text(rule.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(Res.string.ui_1_s_monthly, row.amount),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        if (row.canDelete)
                            ActionMenu(
                                modifier = Modifier.testTag("recurring_actions_${rule.title}"),
                                items =
                                    buildList {
                                        if (row.canManage) {
                                            add(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            Res.string.history_edit,
                                                        ),
                                                    onClick = { onEdit(rule) },
                                                    enabled = !busy,
                                                ),
                                            )
                                            add(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            if (rule.active) Res.string.ui_pause
                                                            else Res.string.ui_resume,
                                                        ),
                                                    onClick = { onControl(rule, row.toggleAction) },
                                                    enabled = !busy,
                                                ),
                                            )
                                        }
                                        add(
                                            ActionMenuItem(
                                                text =
                                                    TextResource.Resource(Res.string.detail_delete),
                                                onClick = { onDeleteRequested(rule) },
                                                enabled = !busy,
                                                destructive = true,
                                            ),
                                        )
                                    },
                            )
                    }
                    Text(
                        when (rule.status) {
                            RecurringExpenseStatusDomain.ACTIVE ->
                                stringResource(Res.string.ui_active)
                            RecurringExpenseStatusDomain.PAUSED ->
                                stringResource(Res.string.ui_paused)
                            RecurringExpenseStatusDomain.CANCELLED ->
                                stringResource(Res.string.ui_cancelled)
                        },
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (rule.active) Text(stringResource(Res.string.ui_next_entry_1_s, rule.nextOn))
                    if (!rule.cancelled)
                        Text(stringResource(Res.string.ui_monthly_day, rule.anchorDay))
                    Text(
                        stringResource(
                            Res.string.ui_payer_1_s,
                            row.payerName ?: stringResource(Res.string.ui_former_member),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (row.isPaused)
                        Text(
                            stringResource(
                                Res.string
                                    .ui_resuming_schedules_the_next_future_entry_paused_months_are,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                }
            }
    }
}
