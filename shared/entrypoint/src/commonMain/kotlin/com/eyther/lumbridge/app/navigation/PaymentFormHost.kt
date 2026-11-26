package com.eyther.lumbridge.app.navigation

import com.eyther.lumbridge.core.domain.today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.model.sheet.PaymentFormSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseDateBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.screen.PaymentFormScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_edit_payment
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_record_payment
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PaymentFormHost(
    accountId: String,
    paymentId: String?,
    suggestion: SuggestedSettlementDomain?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: PaymentFormScreenViewModelContract = koinViewModel {
        parametersOf(
            PaymentFormScreenViewModelContract.Params(
                accountId = accountId,
                paymentId = paymentId,
                suggestion = suggestion,
            ),
        )
    }
    val formState by viewModel.state.platformCollectAsStateOnStart()
    val sheets = rememberBottomSheetController()
    sheets.registerBottomSheet<PaymentFormSheets.Date> {
        ExpenseDateBottomSheet(
            date = formState.occurredOn ?: today(),
            onSelected = { date ->
                viewModel.onDateChanged(date)
                sheets.hideBottomSheet()
            },
            onClose = sheets::hideBottomSheet,
        )
    }
    BottomSheetLayout(controller = sheets, dismissEnabled = !formState.busy, onDismissed = {}) {
        FlowScaffold(
            title =
                stringResource(
                    if (paymentId == null) Res.string.flow_record_payment
                    else Res.string.flow_edit_payment,
                ),
            onBack = onBack,
            backEnabled = !formState.busy,
        ) {
            PaymentFormScreen(
                viewModel = viewModel,
                onSaved = onSaved,
                onChooseDate = { sheets.showBottomSheet(PaymentFormSheets.Date) },
            )
        }
    }
    BusyNavigationGuard(busy = formState.busy)
}
