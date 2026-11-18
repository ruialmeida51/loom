package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.features.expenses.presentation.screen.ReceiptViewerScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ReceiptViewerScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_receipt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ReceiptViewerHost(accountId: String, expenseId: String, onBack: () -> Unit) {
    val viewModel: ReceiptViewerScreenViewModelContract = koinViewModel {
        parametersOf(
            ReceiptViewerScreenViewModelContract.Params(
                accountId = accountId,
                expenseId = expenseId,
            ),
        )
    }
    FlowScaffold(title = stringResource(Res.string.flow_receipt), onBack = onBack) {
        ReceiptViewerScreen(viewModel = viewModel)
    }
}
