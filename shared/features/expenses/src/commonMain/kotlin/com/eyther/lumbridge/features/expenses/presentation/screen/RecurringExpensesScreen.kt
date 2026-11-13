package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.expenses.domain.model.*
import com.eyther.lumbridge.features.expenses.generated.resources.*
import com.eyther.lumbridge.features.expenses.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecurringExpensesScreen(
    vm: RecurringExpensesViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerRecurringExpensesScreen(
        state.rules,
        state.payerNames,
        state.isOwner,
        state.busy,
        state.error,
        context.userId,
        context.currency,
        vm::control,
        vm::retry,
        vm::dismiss,
    )
}

@Composable
private fun InnerRecurringExpensesScreen(
    rules: List<RecurringExpenseDomain>,
    payerNames: Map<String, String>,
    isOwner: Boolean,
    busy: Boolean,
    error: String?,
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
            ErrorNotice(localizedMessage(error), onDismiss)
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
