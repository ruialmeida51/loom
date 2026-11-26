package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.InfoRow
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_everyday
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.detail_account
import com.eyther.lumbridge.features.expenses.generated.resources.detail_date
import com.eyther.lumbridge.features.expenses.generated.resources.detail_paid_by
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseDetailsLoading
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseDetailsSummaryCard
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseShareRow
import com.eyther.lumbridge.features.expenses.presentation.components.receipt.ExpenseReceiptSection
import com.eyther.lumbridge.features.expenses.presentation.components.receipt.rememberReceiptPickers
import com.eyther.lumbridge.features.expenses.presentation.mapper.splitMethodLabel
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseDetailsScreen(
    viewModel: ExpenseDetailsScreenViewModelContract,
    onDeleted: () -> Unit,
    onViewReceipt: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val pickers =
        rememberReceiptPickers(
            onPicked = viewModel::onReceiptPicked,
            onError = viewModel::onReceiptPickFailed,
        )
    LaunchedEffect(state.redirectTo) {
        when (state.redirectTo) {
            ExpenseDetailsScreenViewModelContract.RedirectTo.Deleted -> {
                viewModel.onNavigationHandled()
                onDeleted()
            }
            null -> Unit
        }
    }
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        snackbarHost = { SuccessSnackbar(message = state.successMessage) },
    ) {
        InnerExpenseDetailsScreen(
            details = state.details,
            loading = state.loading,
            error = state.error,
            onRetry = viewModel::onRefresh,
            onDismiss = viewModel::onDismissError,
            receiptPhoto = state.receiptPhotoUi,
            receiptPreview = state.receiptPreviewUi,
            receiptPreviewLoading = state.receiptPreviewLoading,
            receiptAttached = state.receipt?.objectPath != null,
            receiptLoading = state.receiptLoading,
            receiptPreparing = state.receiptPreparing || pickers.reading,
            receiptCanManage = state.canEdit && state.receiptReady,
            receiptBusy = state.busy,
            receiptError = state.receiptError.takeUnless { state.showReceiptDeletion },
            cameraSupported = pickers.camera.supported,
            onReceiptGallery = pickers.gallery,
            onReceiptCamera = pickers.camera.launch,
            onReceiptView = onViewReceipt,
            onReceiptSave = viewModel::onReceiptSave,
            onReceiptDiscard = viewModel::onReceiptDiscarded,
            onReceiptDelete = viewModel::onReceiptDeleteRequested,
            onReceiptRetry = viewModel::onReceiptRetry,
            onInvalidPhoto = { viewModel.onReceiptPickFailed(ReceiptError.InvalidPhoto()) },
        )
    }
    state.receiptDeleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            error = state.receiptError,
            confirmationTag = "confirm_delete_receipt",
            onConfirm = viewModel::onReceiptDeleteConfirmed,
            onDismiss = viewModel::onReceiptDeleteCancelled,
        )
    }
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            confirmationTag = "confirm_delete_expense",
            error = state.deleteError,
            onConfirm = viewModel::onDeleteConfirmed,
            onDismiss = viewModel::onDeleteCancelled,
        )
    }
}

@Composable
private fun InnerExpenseDetailsScreen(
    details: ExpenseDetailsUi?,
    loading: Boolean,
    error: TextResource?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    receiptPhoto: ReceiptPhotoUi?,
    receiptPreview: ReceiptPhotoUi?,
    receiptPreviewLoading: Boolean,
    receiptAttached: Boolean,
    receiptLoading: Boolean,
    receiptPreparing: Boolean,
    receiptCanManage: Boolean,
    receiptBusy: Boolean,
    receiptError: TextResource?,
    cameraSupported: Boolean,
    onReceiptGallery: () -> Unit,
    onReceiptCamera: () -> Unit,
    onReceiptView: () -> Unit,
    onReceiptSave: () -> Unit,
    onReceiptDiscard: () -> Unit,
    onReceiptDelete: () -> Unit,
    onReceiptRetry: () -> Unit,
    onInvalidPhoto: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("expense_details_content"),
        contentPadding = PaddingValues(LoomSpacing.content),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        error?.let { message ->
            item(key = "error") {
                RetryNotice(
                    message = message,
                    onRetry = onRetry,
                    onDismiss = onDismiss,
                    retryText = stringResource(Res.string.balance_retry),
                )
            }
        }
        if (loading) item(key = "loading") { ExpenseDetailsLoading() }
        else
            details?.let { expense ->
                item(key = "summary") { ExpenseDetailsSummaryCard(details = expense) }
                item(key = "context") {
                    GroupedSurface {
                        InfoRow(
                            label = stringResource(Res.string.detail_paid_by),
                            value = expense.payerName ?: stringResource(Res.string.former_member),
                        )
                        LoomDivider()
                        InfoRow(
                            label = stringResource(Res.string.detail_date),
                            value = calendarDateText(expense.occurredOn),
                        )
                        LoomDivider()
                        InfoRow(
                            label = stringResource(Res.string.detail_account),
                            value =
                                if (expense.isEveryday) stringResource(Res.string.account_everyday)
                                else expense.accountName,
                        )
                    }
                }
                item(key = "receipt") {
                    ExpenseReceiptSection(
                        photo = receiptPhoto,
                        preview = receiptPreview,
                        previewLoading = receiptPreviewLoading,
                        attached = receiptAttached,
                        loading = receiptLoading,
                        preparing = receiptPreparing,
                        busy = receiptBusy || receiptPreparing,
                        canManage = receiptCanManage,
                        canDelete = true,
                        cameraSupported = cameraSupported,
                        showDraftActions = true,
                        error = receiptError,
                        onGallery = onReceiptGallery,
                        onCamera = onReceiptCamera,
                        onView = onReceiptView,
                        onDiscard = onReceiptDiscard,
                        onSave = onReceiptSave,
                        onDelete = onReceiptDelete,
                        onRetry = onReceiptRetry,
                        onInvalidPhoto = onInvalidPhoto,
                    )
                }
                item(key = "split_header") {
                    SectionTitle(title = stringResource(Res.string.form_split))
                }
                item(key = "shares") {
                    GroupedSurface {
                        Text(
                            text = splitMethodLabel(expense.splitMethod),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(LoomSpacing.content),
                        )
                        expense.shares.forEachIndexed { index, share ->
                            key(share.id) {
                                if (index > 0)
                                    LoomDivider()
                                ExpenseShareRow(share = share)
                            }
                        }
                    }
                }
            }
    }
}
