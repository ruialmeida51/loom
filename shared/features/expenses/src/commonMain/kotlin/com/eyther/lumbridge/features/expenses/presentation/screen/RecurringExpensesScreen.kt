package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseStatusDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_1_s_monthly
import com.eyther.lumbridge.features.expenses.generated.resources.ui_active
import com.eyther.lumbridge.features.expenses.generated.resources.ui_add_an_expense_in_money_and_choose_monthly_repeat
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel_schedule
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel_this_schedule
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancelled
import com.eyther.lumbridge.features.expenses.generated.resources.ui_each_active_schedule_records_an_expense_on_its_due
import com.eyther.lumbridge.features.expenses.generated.resources.ui_every_month_sorted
import com.eyther.lumbridge.features.expenses.generated.resources.ui_former_member
import com.eyther.lumbridge.features.expenses.generated.resources.ui_future_entries_will_stop_expenses_already_recorded_stay_in
import com.eyther.lumbridge.features.expenses.generated.resources.ui_keep_schedule
import com.eyther.lumbridge.features.expenses.generated.resources.ui_manage_the_fixed_expenses_you_share_at_home
import com.eyther.lumbridge.features.expenses.generated.resources.ui_monthly_day
import com.eyther.lumbridge.features.expenses.generated.resources.ui_next_entry_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_no_recurring_expenses_yet
import com.eyther.lumbridge.features.expenses.generated.resources.ui_pause
import com.eyther.lumbridge.features.expenses.generated.resources.ui_paused
import com.eyther.lumbridge.features.expenses.generated.resources.ui_payer_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_recurring_expenses
import com.eyther.lumbridge.features.expenses.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.expenses.generated.resources.ui_rent_internet_and_subscriptions_can_live_here
import com.eyther.lumbridge.features.expenses.generated.resources.ui_resume
import com.eyther.lumbridge.features.expenses.generated.resources.ui_resuming_schedules_the_next_future_entry_paused_months_are
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecurringExpensesScreen(
    viewModel: RecurringExpensesScreenViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerRecurringExpensesScreen(
        rules = state.rules,
        payerNames = state.payerNames,
        isOwner = state.isOwner,
        busy = state.busy,
        error = state.error,
        userId = context.userId,
        currency = context.currency,
        onControl = viewModel::onControlSchedule,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerRecurringExpensesScreen(
    rules: List<RecurringExpenseDomain>,
    payerNames: Map<String, String>,
    isOwner: Boolean,
    busy: Boolean,
    error: TextResource?,
    userId: String,
    currency: String,
    onControl: (RecurringExpenseDomain, RecurringExpenseActionDomain) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    var cancellation by remember { mutableStateOf<RecurringExpenseDomain?>(null) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp).testTag("recurring_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_every_month_sorted),
                stringResource(Res.string.ui_recurring_expenses),
                stringResource(Res.string.ui_manage_the_fixed_expenses_you_share_at_home),
            )
        }
        item {
            Text(
                stringResource(Res.string.ui_add_an_expense_in_money_and_choose_monthly_repeat),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(Res.string.ui_each_active_schedule_records_an_expense_on_its_due),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            ErrorNotice(error, onDismiss)
            if (error != null)
                TextButton(onClick = onRetry) {
                    Text(stringResource(Res.string.ui_refresh_and_retry))
                }
        }
        if (rules.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_no_recurring_expenses_yet),
                    stringResource(Res.string.ui_rent_internet_and_subscriptions_can_live_here),
                )
            }
        items(rules, key = { it.id }) { rule ->
            LoomCard {
                Text(rule.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(
                        Res.string.ui_1_s_monthly,
                        MoneyDomain(rule.amountMinor, currency).formatted(),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    when (rule.status) {
                        RecurringExpenseStatusDomain.ACTIVE -> stringResource(Res.string.ui_active)
                        RecurringExpenseStatusDomain.PAUSED -> stringResource(Res.string.ui_paused)
                        RecurringExpenseStatusDomain.CANCELLED ->
                            stringResource(Res.string.ui_cancelled)
                    },
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (rule.active) Text(stringResource(Res.string.ui_next_entry_1_s, rule.nextOn))
                if (!rule.cancelled) Text(stringResource(Res.string.ui_monthly_day, rule.anchorDay))
                Text(
                    stringResource(
                        Res.string.ui_payer_1_s,
                        payerNames[rule.paidBy] ?: stringResource(Res.string.ui_former_member),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (rule.canManage(userId, isOwner))
                    Row {
                        TextButton(
                            onClick = {
                                onControl(
                                    rule,
                                    if (rule.active) RecurringExpenseActionDomain.PAUSE
                                    else RecurringExpenseActionDomain.RESUME,
                                )
                            },
                            enabled = !busy,
                            modifier = Modifier.testTag("toggle_recurring_${rule.title}"),
                        ) {
                            Text(
                                if (rule.active) stringResource(Res.string.ui_pause)
                                else stringResource(Res.string.ui_resume),
                            )
                        }
                        TextButton(
                            onClick = { cancellation = rule },
                            enabled = !busy,
                            modifier = Modifier.testTag("cancel_recurring_${rule.title}"),
                        ) {
                            Text(stringResource(Res.string.ui_cancel_schedule))
                        }
                    }
                if (!rule.active && !rule.cancelled)
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
    cancellation?.let { rule ->
        AlertDialog(
            onDismissRequest = { cancellation = null },
            title = { Text(stringResource(Res.string.ui_cancel_this_schedule)) },
            text = {
                Text(
                    stringResource(
                        Res.string.ui_future_entries_will_stop_expenses_already_recorded_stay_in,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cancellation = null
                        onControl(rule, RecurringExpenseActionDomain.CANCEL)
                    },
                    enabled = !busy,
                    modifier = Modifier.testTag("confirm_cancel_recurring"),
                ) {
                    Text(stringResource(Res.string.ui_cancel_schedule))
                }
            },
            dismissButton = {
                TextButton(onClick = { cancellation = null }) {
                    Text(stringResource(Res.string.ui_keep_schedule))
                }
            },
        )
    }
}
