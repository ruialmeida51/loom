package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingForm
import com.eyther.lumbridge.core.presentation.components.LoadingRow
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.form_category
import com.eyther.lumbridge.features.expenses.generated.resources.form_date
import com.eyther.lumbridge.features.expenses.generated.resources.form_monthly
import com.eyther.lumbridge.features.expenses.generated.resources.form_recurring_unchanged
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_keep_expense
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_pending
import com.eyther.lumbridge.features.expenses.generated.resources.split_participant_count
import com.eyther.lumbridge.features.expenses.generated.resources.split_summary
import com.eyther.lumbridge.features.expenses.generated.resources.ui_who_paid
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseCaptureFields
import com.eyther.lumbridge.features.expenses.presentation.components.receipt.ExpenseReceiptSection
import com.eyther.lumbridge.features.expenses.presentation.components.receipt.rememberReceiptPickers
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseCategoryLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.splitMethodLabel
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPickerRow
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseFormScreen(
    viewModel: ExpenseFormScreenViewModelContract,
    onSaved: () -> Unit,
    onEditSplit: (Long) -> Unit,
    onChooseDate: () -> Unit,
    onViewReceipt: () -> Unit = {},
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val pickers =
        rememberReceiptPickers(
            onPicked = viewModel::onReceiptPicked,
            onError = viewModel::onReceiptPickFailed,
        )
    LaunchedEffect(state.redirectTo) {
        when (val redirect = state.redirectTo) {
            ExpenseFormScreenViewModelContract.RedirectTo.Saved -> {
                viewModel.onNavigationHandled()
                onSaved()
            }
            is ExpenseFormScreenViewModelContract.RedirectTo.EditSplit -> {
                viewModel.onNavigationHandled()
                onEditSplit(redirect.amountMinor)
            }
            null -> Unit
        }
    }
    InnerExpenseFormScreen(
        account = state.account,
        members = state.members,
        title = state.title,
        amount = state.amount,
        paidBy = state.paidBy,
        date = state.occurredOn,
        category = state.category,
        recurring = state.recurring,
        splitMethod = state.split.method,
        participantCount = state.split.participants.size,
        editing = state.editing,
        readyForInput = !state.editing || state.expense != null,
        loading = state.loading,
        busy = state.busy,
        canSave = state.canSave && !pickers.reading,
        error = state.error,
        onTitle = viewModel::onTitleChanged,
        onAmount = viewModel::onAmountChanged,
        onPayer = viewModel::onPayerChanged,
        onCategory = viewModel::onCategoryChanged,
        onRecurring = viewModel::onRecurringChanged,
        onSplit = viewModel::onEditSplit,
        onDate = onChooseDate,
        onSave = viewModel::onSave,
        onDismiss = viewModel::onDismissError,
        onRetry = viewModel::onRetry,
        receiptPhoto = state.receiptPhotoUi,
        receiptAttached = state.receipt?.objectPath != null,
        receiptLoading = state.receiptLoading,
        receiptPreparing = state.receiptPreparing || pickers.reading,
        receiptReady = state.receiptReady,
        receiptPending = state.receiptPending,
        receiptError = state.receiptError,
        cameraSupported = pickers.camera.supported,
        onReceiptGallery = pickers.gallery,
        onReceiptCamera = pickers.camera.launch,
        onReceiptView = onViewReceipt,
        onReceiptDiscard = viewModel::onReceiptDiscarded,
        onReceiptRetry = viewModel::onReceiptRetry,
        onKeepExpense = viewModel::onKeepExpense,
        onInvalidPhoto = { viewModel.onReceiptPickFailed(ReceiptError.InvalidPhoto()) },
    )
}

@Composable
private fun InnerExpenseFormScreen(
    account: ExpenseAccountDomain?,
    members: List<MemberDomain>,
    title: String,
    amount: String,
    paidBy: String,
    date: String,
    category: String,
    recurring: Boolean,
    splitMethod: ExpenseSplitMethodDomain,
    participantCount: Int,
    editing: Boolean,
    readyForInput: Boolean,
    loading: Boolean,
    busy: Boolean,
    canSave: Boolean,
    error: TextResource?,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onPayer: (String) -> Unit,
    onCategory: (String) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onSplit: () -> Unit,
    onDate: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    receiptPhoto: ReceiptPhotoUi?,
    receiptAttached: Boolean,
    receiptLoading: Boolean,
    receiptPreparing: Boolean,
    receiptReady: Boolean,
    receiptPending: Boolean,
    receiptError: TextResource?,
    cameraSupported: Boolean,
    onReceiptGallery: () -> Unit,
    onReceiptCamera: () -> Unit,
    onReceiptView: () -> Unit,
    onReceiptDiscard: () -> Unit,
    onReceiptRetry: () -> Unit,
    onKeepExpense: () -> Unit,
    onInvalidPhoto: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            if (readyForInput) {
                ExpenseCaptureFields(
                    amount = amount,
                    title = title,
                    currency = account?.currency,
                    enabled = !busy && !receiptPending,
                    onAmount = onAmount,
                    onTitle = onTitle,
                )
            } else if (loading) LoadingForm()
            error?.let { message ->
                ErrorNotice(message = message, onDismiss = if (account == null) null else onDismiss)
                if (account == null)
                    LoomSecondaryButton(
                        text = stringResource(Res.string.balance_retry),
                        onClick = onRetry,
                        modifier = Modifier,
                    )
            }
            if (loading && account == null && readyForInput) LoadingRow()
            if (account != null) {
                Text(
                    accountLabel(account),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GroupedSurface {
                    MemberPickerRow(
                        label = stringResource(Res.string.ui_who_paid),
                        members = members,
                        selectedId = paidBy,
                        onSelected = onPayer,
                        enabled = !busy && !receiptPending,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.form_split),
                        value =
                            stringResource(
                                Res.string.split_summary,
                                splitMethodLabel(splitMethod),
                                pluralStringResource(
                                    Res.plurals.split_participant_count,
                                    participantCount,
                                    participantCount,
                                ),
                            ),
                        onClick = onSplit,
                        enabled = !busy && !receiptPending,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.form_date),
                        description = date,
                        onClick = onDate,
                        enabled = !busy && !receiptPending,
                    )
                }
                Text(
                    text = stringResource(Res.string.form_category),
                    style = MaterialTheme.typography.labelLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                ) {
                    listOf("Groceries", "Bills", "Food & drink", "Home", "Other").forEach { choice
                        ->
                        FilterChip(
                            selected = category == choice,
                            onClick = { onCategory(choice) },
                            enabled = !busy && !receiptPending,
                            label = { Text(expenseCategoryLabel(choice)) },
                        )
                    }
                }
                ExpenseReceiptSection(
                    photo = receiptPhoto,
                    attached = receiptAttached,
                    loading = receiptLoading,
                    preparing = receiptPreparing,
                    busy = busy || receiptPending || receiptPreparing,
                    canManage = receiptReady,
                    canDelete = false,
                    cameraSupported = cameraSupported,
                    showDraftActions = false,
                    error = receiptError,
                    onGallery = onReceiptGallery,
                    onCamera = onReceiptCamera,
                    onView = onReceiptView,
                    onDiscard = onReceiptDiscard,
                    onSave = onSave,
                    onDelete = {},
                    onRetry = onReceiptRetry,
                    onInvalidPhoto = onInvalidPhoto,
                )
                if (receiptPending && !busy) {
                    Text(
                        stringResource(Res.string.receipt_pending),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    LoomSecondaryButton(
                        text = stringResource(Res.string.receipt_keep_expense),
                        onClick = onKeepExpense,
                        modifier = Modifier.testTag("keep_expense"),
                    )
                }
                if (!editing)
                    Row {
                        Text(
                            stringResource(Res.string.form_monthly),
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            modifier = Modifier.testTag("expense_recurring"),
                            checked = recurring,
                            onCheckedChange = onRecurring,
                            enabled = !busy && !receiptPending,
                        )
                    }
                else if (recurring)
                    Text(
                        stringResource(Res.string.form_recurring_unchanged),
                        style = MaterialTheme.typography.bodySmall,
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
                    .testTag("save_expense")
                    .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
        )
    }
}
