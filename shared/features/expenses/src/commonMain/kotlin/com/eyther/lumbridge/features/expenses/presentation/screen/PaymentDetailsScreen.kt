package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.InfoRow
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_everyday
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.detail_account
import com.eyther.lumbridge.features.expenses.generated.resources.detail_date
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.ui_money_sent_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_received_by
import com.eyther.lumbridge.features.expenses.presentation.components.PaymentDetailsLoading
import com.eyther.lumbridge.features.expenses.presentation.components.PaymentDetailsSummaryCard
import com.eyther.lumbridge.features.expenses.presentation.model.PaymentDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentDetailsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

/** Renders a scoped payment and its confirmation; the Host owns editing and return navigation. */
@Composable
fun PaymentDetailsScreen(viewModel: PaymentDetailsScreenViewModelContract, onDeleted: () -> Unit) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.redirectTo) {
        when (state.redirectTo) {
            PaymentDetailsScreenViewModelContract.RedirectTo.Deleted -> {
                viewModel.onNavigationHandled()
                onDeleted()
            }
            null -> Unit
        }
    }
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
    ) {
        InnerPaymentDetailsScreen(
            details = state.details,
            loading = state.loading,
            error = state.error,
            onRetry = viewModel::onRefresh,
            onDismiss = viewModel::onDismissError,
        )
    }
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            error = state.deleteError,
            confirmationTag = "confirm_delete_payment_details",
            onConfirm = viewModel::onDeleteConfirmed,
            onDismiss = viewModel::onDeleteCancelled,
        )
    }
}

@Composable
private fun InnerPaymentDetailsScreen(
    details: PaymentDetailsUi?,
    loading: Boolean,
    error: TextResource?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("payment_details_content"),
        contentPadding = PaddingValues(LoomSpacing.content),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
                LoomSecondaryButton(
                    text = stringResource(Res.string.balance_retry),
                    onClick = onRetry,
                )
            }
        }
        if (loading) item(key = "loading") { PaymentDetailsLoading() }
        else
            details?.let { payment ->
                item(key = "summary") { PaymentDetailsSummaryCard(details = payment) }
                item(key = "context") {
                    GroupedSurface {
                        InfoRow(
                            label = stringResource(Res.string.ui_money_sent_by),
                            value = payment.fromName ?: stringResource(Res.string.former_member),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(
                            label = stringResource(Res.string.ui_received_by),
                            value = payment.toName ?: stringResource(Res.string.former_member),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(
                            label = stringResource(Res.string.detail_date),
                            value = calendarDateText(payment.occurredOn),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(
                            label = stringResource(Res.string.detail_account),
                            value =
                                if (payment.isEveryday) stringResource(Res.string.account_everyday)
                                else payment.accountName,
                        )
                    }
                }
            }
    }
}
