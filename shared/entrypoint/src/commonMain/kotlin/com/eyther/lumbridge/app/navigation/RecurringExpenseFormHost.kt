package com.eyther.lumbridge.app.navigation

import com.eyther.lumbridge.core.domain.today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.sheet.RecurringExpenseFormSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseDateBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseSplitBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.screen.RecurringExpenseFormScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseSplitViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpenseFormScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.bill_edit_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The expense draft outlives transient split/date editors; dismissing a split never commits it. */
@Composable
internal fun RecurringExpenseFormHost(
    accountId: String,
    ruleId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: RecurringExpenseFormScreenViewModelContract = koinViewModel {
        parametersOf(
            RecurringExpenseFormScreenViewModelContract.Params(
                accountId = accountId,
                ruleId = ruleId,
            ),
        )
    }
    val formState by viewModel.state.platformCollectAsStateOnStart()
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.RECURRING_EXPENSES)
    val sheets = rememberBottomSheetController()
    sheets.registerBottomSheet<RecurringExpenseFormSheets.Date> {
        ExpenseDateBottomSheet(
            date = formState.nextOn ?: today(),
            onSelected = { date ->
                viewModel.onDateChanged(date)
                sheets.hideBottomSheet()
            },
            onClose = sheets::hideBottomSheet,
        )
    }
    sheets.registerBottomSheet<RecurringExpenseFormSheets.Split> { sheet ->
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
            title = stringResource(Res.string.bill_edit_title),
            onBack = onBack,
            backEnabled = !formState.busy,
        ) {
            RecurringExpenseFormScreen(
                viewModel = viewModel,
                onSaved = onSaved,
                onSplit = { amount ->
                    sheets.showBottomSheet(RecurringExpenseFormSheets.Split(amountMinor = amount))
                },
                onDate = { sheets.showBottomSheet(RecurringExpenseFormSheets.Date) },
            )
        }
    }
    BusyNavigationGuard(busy = formState.busy)
}
