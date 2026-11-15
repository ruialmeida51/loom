package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.ExpensesSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseAccountsBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.SettlementBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract

@Composable
internal fun ExpensesSheetHost(
    viewModel: ExpensesScreenViewModelContract,
    onCreateAccount: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requested =
        when {
            state.showSettle -> ExpensesSheets.Settlement
            state.showAccounts -> ExpensesSheets.Accounts
            else -> null
        }
    LaunchedEffect(requested) {
        if (requested == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requested)
    }
    controller.registerBottomSheet<ExpensesSheets.Settlement> {
        SettlementBottomSheet(
            members = state.accountMembers,
            from = state.from,
            to = state.to,
            settleAmount = state.settleAmount,
            busy = state.busy,
            formError = state.formError,
            currency = state.currency,
            onShowSettle = viewModel::onSettlementSheetVisibilityChanged,
            onFrom = viewModel::onFromMemberChanged,
            onTo = viewModel::onToMemberChanged,
            onSettleAmount = viewModel::onSettlementAmountChanged,
            onSettle = viewModel::onRecordSettlement,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ExpensesSheets.Accounts> {
        ExpenseAccountsBottomSheet(
            active = state.activeAccounts,
            archived = state.archivedAccounts,
            busy = state.busy,
            onSelect = viewModel::onAccountSelected,
            onCreate = {
                viewModel.onAccountsVisibilityChanged(false)
                onCreateAccount()
            },
            onClose = { viewModel.onAccountsVisibilityChanged(false) },
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            if (state.showSettle) viewModel.onSettlementSheetVisibilityChanged(false)
            else viewModel.onAccountsVisibilityChanged(false)
        },
        content = content,
    )
}
