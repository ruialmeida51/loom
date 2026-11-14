package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.ExpensesSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.SettlementBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.SharedExpenseBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain

@Composable
internal fun ExpensesSheetHost(
    viewModel: ExpensesScreenViewModelContract,
    context: HouseholdContextDomain,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showSettle -> ExpensesSheets.Settlement
            state.showAdd -> ExpensesSheets.SharedExpense
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<ExpensesSheets.SharedExpense> {
        SharedExpenseBottomSheet(
            members = state.members,
            title = state.title,
            amount = state.amount,
            occurredOn = state.occurredOn,
            paidBy = state.paidBy,
            selectedCategory = state.category,
            selected = state.selected,
            splitMode = state.splitMode,
            custom = state.custom,
            recurring = state.recurring,
            busy = state.busy,
            formError = state.formError,
            context = context,
            onTitle = viewModel::onTitleChanged,
            onAmount = viewModel::onAmountChanged,
            onOccurredOn = viewModel::onDateChanged,
            onPayer = viewModel::onPayerChanged,
            onCategory = viewModel::onCategoryChanged,
            onSelected = viewModel::onParticipantToggled,
            onSplitMode = viewModel::onSplitModeChanged,
            onCustom = viewModel::onCustomShareChanged,
            onRecurring = viewModel::onRecurringChanged,
            onShowAdd = viewModel::onAddSheetVisibilityChanged,
            onAdd = viewModel::onSaveExpense,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ExpensesSheets.Settlement> {
        SettlementBottomSheet(
            members = state.members,
            from = state.from,
            to = state.to,
            settleAmount = state.settleAmount,
            busy = state.busy,
            formError = state.formError,
            context = context,
            onShowSettle = viewModel::onSettlementSheetVisibilityChanged,
            onFrom = viewModel::onFromMemberChanged,
            onTo = viewModel::onToMemberChanged,
            onSettleAmount = viewModel::onSettlementAmountChanged,
            onSettle = viewModel::onRecordSettlement,
            onDismiss = viewModel::onDismissError,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            if (state.showSettle) viewModel.onSettlementSheetVisibilityChanged(false)
            else viewModel.onAddSheetVisibilityChanged(false)
        },
        content = content,
    )
}
