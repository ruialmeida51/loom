package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.sheet.ExpenseFormSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseDateBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseSplitBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseFormScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseSplitViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_add_expense
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_edit_expense
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The expense draft outlives transient split/date editors; dismissing a split never commits it. */
@Composable
internal fun ExpenseFormHost(
    accountId: String,
    expenseId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onViewReceipt: () -> Unit = {},
) {
    val viewModel: ExpenseFormScreenViewModelContract = koinViewModel {
        parametersOf(
            ExpenseFormScreenViewModelContract.Params(accountId = accountId, expenseId = expenseId),
        )
    }
    val formState by viewModel.state.platformCollectAsStateOnStart()
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.EXPENSE_FORM)
    val sheets = rememberBottomSheetController()
    sheets.registerBottomSheet<ExpenseFormSheets.Date> {
        ExpenseDateBottomSheet(
            date = formState.occurredOn,
            onSelected = { date ->
                viewModel.onDateChanged(date)
                sheets.hideBottomSheet()
            },
            onClose = sheets::hideBottomSheet,
        )
    }
    sheets.registerBottomSheet<ExpenseFormSheets.Split> { sheet ->
        val splitOwner = rememberHostViewModelStoreOwner()
        val splitViewModel: ExpenseSplitViewModelContract =
            koinViewModel(viewModelStoreOwner = splitOwner) {
                parametersOf(
                    ExpenseSplitViewModelContract.Params(
                        amountMinor = sheet.amountMinor,
                        currency = formState.currency,
                        members = formState.members,
                        selection = formState.split,
                    ),
                )
            }
        ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.EXPENSE_SPLIT)
        ExpenseSplitBottomSheet(
            viewModel = splitViewModel,
            onConfirmed = { selection ->
                viewModel.onSplitConfirmed(selection)
                sheets.hideBottomSheet()
            },
            onClose = sheets::hideBottomSheet,
        )
    }
    BottomSheetLayout(controller = sheets, dismissEnabled = !formState.busy, onDismissed = {}) {
        FlowScaffold(
            title =
                stringResource(
                    if (expenseId == null) Res.string.flow_add_expense
                    else Res.string.flow_edit_expense,
                ),
            onBack = onBack,
            backEnabled = !formState.busy,
        ) {
            ExpenseFormScreen(
                viewModel = viewModel,
                onSaved = onSaved,
                onViewReceipt = onViewReceipt,
                onEditSplit = { amount ->
                    sheets.showBottomSheet(ExpenseFormSheets.Split(amountMinor = amount))
                },
                onChooseDate = { sheets.showBottomSheet(ExpenseFormSheets.Date) },
            )
        }
    }
    BusyNavigationGuard(busy = formState.busy)
}
