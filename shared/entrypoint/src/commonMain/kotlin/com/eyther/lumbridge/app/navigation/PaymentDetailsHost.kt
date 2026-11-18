package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.presentation.components.FinancialRecordActions
import com.eyther.lumbridge.features.expenses.presentation.screen.PaymentDetailsScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentDetailsScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_payment_details
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PaymentDetailsHost(
    accountId: String,
    paymentId: String,
    contentRevision: Int,
    active: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel: PaymentDetailsScreenViewModelContract = koinViewModel {
        parametersOf(
            PaymentDetailsScreenViewModelContract.Params(
                accountId = accountId,
                paymentId = paymentId,
            ),
        )
    }
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(active, contentRevision) {
        if (active) viewModel.onContentChanged()
    }
    FlowScaffold(
        title = stringResource(Res.string.flow_payment_details),
        onBack = onBack,
        backEnabled = !state.busy,
        actions = {
            FinancialRecordActions(
                testTag = "payment_details_actions",
                canEdit = state.canEdit,
                busy = state.busy,
                onEdit = onEdit,
                onDelete = viewModel::onDeleteRequested,
            )
        },
    ) {
        PaymentDetailsScreen(viewModel = viewModel, onDeleted = onDeleted)
    }
    BusyNavigationGuard(busy = state.busy)
}
