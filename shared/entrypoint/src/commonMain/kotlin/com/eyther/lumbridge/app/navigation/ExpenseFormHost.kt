package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.ExpenseFormDestination
import com.eyther.lumbridge.app.navigation.model.sheet.ExpenseFormSheets
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseDateBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseFormScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.SplitScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.SplitScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_add_expense
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_edit_expense
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_split
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ExpenseFormHost(
    accountId: String,
    expenseId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: ExpenseFormScreenViewModelContract = koinViewModel {
        parametersOf(
            ExpenseFormScreenViewModelContract.Params(accountId = accountId, expenseId = expenseId),
        )
    }
    val formState by viewModel.state.platformCollectAsStateOnStart()
    val navigationState =
        rememberNavigationState(startRoot = ExpenseFormDestination.Form) {
            subclass(ExpenseFormDestination.Form::class)
            subclass(ExpenseFormDestination.Split::class)
        }
    val navigator = remember(navigationState) { Navigator(state = navigationState) }
    ScreenAnalyticsEffect(
        screen =
            when (navigationState.getActiveBackStack()?.lastOrNull()) {
                ExpenseFormDestination.Form -> AnalyticsScreenDomain.EXPENSE_FORM
                is ExpenseFormDestination.Split -> AnalyticsScreenDomain.EXPENSE_SPLIT
                else -> null
            },
    )
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
    BottomSheetLayout(controller = sheets, dismissEnabled = !formState.busy, onDismissed = {}) {
        HorizontalSlideNavDisplay(navigator = navigator, navigationState = navigationState) { key ->
            when (key) {
                ExpenseFormDestination.Form ->
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
                            onEditSplit = { amount ->
                                navigator.navigateTo(
                                    ExpenseFormDestination.Split(amountMinor = amount),
                                )
                            },
                            onChooseDate = { sheets.showBottomSheet(ExpenseFormSheets.Date) },
                        )
                    }
                is ExpenseFormDestination.Split -> {
                    val splitViewModel: SplitScreenViewModelContract = koinViewModel {
                        parametersOf(
                            SplitScreenViewModelContract.Params(
                                amountMinor = key.amountMinor,
                                currency = formState.currency,
                                members = formState.members,
                                selection = formState.split,
                            ),
                        )
                    }
                    FlowScaffold(
                        title = stringResource(Res.string.flow_split),
                        onBack = navigator::goBack,
                    ) {
                        SplitScreen(
                            viewModel = splitViewModel,
                            onConfirmed = { selection ->
                                viewModel.onSplitConfirmed(selection)
                                navigator.goBack()
                            },
                        )
                    }
                }
            }
        }
    }
    BusyNavigationGuard(busy = formState.busy)
}
