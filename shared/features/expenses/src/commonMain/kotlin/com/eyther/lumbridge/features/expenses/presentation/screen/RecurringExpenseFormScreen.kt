package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingForm
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.bill_future_only
import com.eyther.lumbridge.features.expenses.generated.resources.bill_next_date
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.ui_who_paid
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseCaptureFields
import com.eyther.lumbridge.features.expenses.presentation.components.guestQualifier
import com.eyther.lumbridge.features.expenses.presentation.mapper.splitMethodLabel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPickerRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecurringExpenseFormScreen(
    viewModel: RecurringExpenseFormScreenViewModelContract,
    onSaved: () -> Unit,
    onDate: () -> Unit,
    onSplit: (Long) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    InnerRecurringExpenseFormScreen(
        title = state.title,
        amount = state.amount,
        currency = state.currency,
        paidBy = state.paidBy,
        members = state.payerMembers,
        split = state.split,
        nextOn = state.nextOn,
        loading = state.loading,
        ready = state.rule != null,
        busy = state.busy,
        canSave = state.canSave,
        error = state.error,
        splitEnabled = state.amountMinor != null,
        onTitle = viewModel::onTitleChanged,
        onAmount = viewModel::onAmountChanged,
        onPayer = viewModel::onPayerChanged,
        onDate = onDate,
        onSplit = { state.amountMinor?.let(onSplit) },
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerRecurringExpenseFormScreen(
    title: String,
    amount: String,
    currency: String,
    paidBy: String,
    members: ImmutableList<MemberDomain>,
    split: ExpenseSplitSelectionDomain,
    nextOn: String,
    loading: Boolean,
    ready: Boolean,
    busy: Boolean,
    canSave: Boolean,
    splitEnabled: Boolean,
    error: TextResource?,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onPayer: (String) -> Unit,
    onDate: () -> Unit,
    onSplit: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding().testTag("recurring_form_content")) {
        Column(
            modifier =
                Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
        ) {
            if (loading) LoadingForm()
            else if (ready) {
                ExpenseCaptureFields(
                    amount = amount,
                    title = title,
                    currency = currency,
                    enabled = !busy,
                    onAmount = onAmount,
                    onTitle = onTitle,
                )
                GroupedSurface {
                    MemberPickerRow(
                        label = stringResource(Res.string.ui_who_paid),
                        members = members,
                        qualifier = guestQualifier(),
                        selectedId = paidBy,
                        onSelected = onPayer,
                        enabled = !busy,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.form_split),
                        value = splitMethodLabel(split.method),
                        glyph = Glyph.PERSON,
                        onClick = onSplit,
                        enabled = !busy && splitEnabled,
                        modifier = Modifier.testTag("recurring_split"),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.bill_next_date),
                        description = calendarDateText(LocalDate.parse(nextOn)),
                        glyph = Glyph.CALENDAR,
                        onClick = onDate,
                        enabled = !busy,
                        modifier = Modifier.testTag("recurring_date"),
                    )
                }
                Text(
                    stringResource(Res.string.bill_future_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            error?.let { ErrorNotice(message = it, onDismiss = if (ready) onDismiss else null) }
            if (!ready && !loading)
                LoomSecondaryButton(
                    text = stringResource(Res.string.balance_retry),
                    onClick = onRetry,
                )
        }
        LoomButton(
            text = stringResource(Res.string.form_save),
            onClick = onSave,
            enabled = canSave,
            loading = busy,
            modifier =
                Modifier.fillMaxWidth().padding(LoomSpacing.content).testTag("save_recurring"),
        )
    }
}
