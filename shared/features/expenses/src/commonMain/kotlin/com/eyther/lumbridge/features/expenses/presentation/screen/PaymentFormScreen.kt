package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
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
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.form_date
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.ui_amount_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_money_sent_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_received_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_this_records_a_payment_you_ve_already_made_or
import com.eyther.lumbridge.features.expenses.presentation.components.PayWithSection
import com.eyther.lumbridge.features.expenses.presentation.components.guestQualifier
import com.eyther.lumbridge.features.expenses.presentation.model.PayOptionsUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPickerRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Renders one ViewModel-owned draft; the Host owns date navigation and handles a successful Save.
 */
@Composable
fun PaymentFormScreen(
    viewModel: PaymentFormScreenViewModelContract,
    onSaved: () -> Unit,
    onChooseDate: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }
    InnerPaymentFormScreen(
        members = state.members,
        payOptions = state.payOptions,
        userId = state.userId,
        currency = state.currency,
        from = state.from,
        to = state.to,
        amount = state.amount,
        date = state.date,
        loading = state.loading,
        busy = state.busy,
        canSave = state.canSave,
        error = state.error,
        onFrom = viewModel::onFromChanged,
        onTo = viewModel::onToChanged,
        onAmount = viewModel::onAmountChanged,
        onDate = onChooseDate,
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerPaymentFormScreen(
    members: ImmutableList<MemberDomain>,
    payOptions: PayOptionsUi?,
    userId: String,
    currency: String,
    from: String,
    to: String,
    amount: String,
    date: LocalDate?,
    loading: Boolean,
    busy: Boolean,
    canSave: Boolean,
    error: TextResource?,
    onFrom: (String) -> Unit,
    onTo: (String) -> Unit,
    onAmount: (String) -> Unit,
    onDate: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding().testTag("payment_form_content")) {
        Column(
            modifier =
                Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            if (loading) LoadingForm()
            else if (members.isNotEmpty()) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmount,
                    label = { Text(stringResource(Res.string.ui_amount_1_s, currency)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("repayment_amount"),
                    enabled = !busy,
                )
                GroupedSurface {
                    MemberPickerRow(
                        label = stringResource(Res.string.ui_money_sent_by),
                        members = members,
                        qualifier = guestQualifier(),
                        selectedId = from,
                        currentUserId = userId,
                        onSelected = onFrom,
                        enabled = !busy,
                        emphasisColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("payment_sender"),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    MemberPickerRow(
                        label = stringResource(Res.string.ui_received_by),
                        members = members,
                        qualifier = guestQualifier(),
                        selectedId = to,
                        currentUserId = userId,
                        onSelected = onTo,
                        enabled = !busy,
                        emphasisColor = LoomColors.positive,
                        modifier = Modifier.testTag("payment_recipient"),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.form_date),
                        description = date?.let { calendarDateText(it) },
                        glyph = Glyph.CALENDAR,
                        onClick = onDate,
                        enabled = !busy,
                        modifier = Modifier.testTag("repayment_date"),
                    )
                }
                payOptions?.let { PayWithSection(options = it) }
                Text(
                    stringResource(Res.string.ui_this_records_a_payment_you_ve_already_made_or),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            error?.let { message ->
                ErrorNotice(
                    message = message,
                    onDismiss = if (members.isEmpty()) null else onDismiss,
                )
                if (members.isEmpty())
                    LoomSecondaryButton(
                        text = stringResource(Res.string.balance_retry),
                        onClick = onRetry,
                        enabled = !loading,
                    )
            }
        }
        LoomButton(
            text = stringResource(Res.string.form_save),
            onClick = onSave,
            enabled = canSave,
            loading = busy,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium)
                    .testTag("record_repayment_submit"),
        )
    }
}
