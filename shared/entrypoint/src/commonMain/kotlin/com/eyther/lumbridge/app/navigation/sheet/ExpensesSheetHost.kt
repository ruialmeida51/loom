package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.model.sheet.ExpensesSheets
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseAccountsBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ExpenseDateBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.ManageExpenseAccountsBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet.SettlementBottomSheet
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract

@Composable
internal fun ExpensesSheetHost(
    viewModel: ExpensesScreenViewModelContract,
    onCreateAccount: () -> Unit,
    onEditAccount: (ExpenseAccountDomain) -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requested =
        when {
            state.showSettlementDate -> ExpensesSheets.SettlementDate
            state.showSettle -> ExpensesSheets.Settlement
            state.showAccounts -> ExpensesSheets.Accounts
            state.showManageAccounts -> ExpensesSheets.ManageAccounts
            else -> null
        }
    LaunchedEffect(requested) {
        if (requested == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requested)
    }
    controller.registerBottomSheet<ExpensesSheets.Settlement> {
        SettlementBottomSheet(
            currentUserId = state.userId,
            editing = state.editingSettlement,
            date = state.settlementDate,
            onDate = { viewModel.onSettlementDateVisibilityChanged(true) },
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
    controller.registerBottomSheet<ExpensesSheets.SettlementDate> {
        ExpenseDateBottomSheet(
            date = state.settlementDate,
            onSelected = viewModel::onSettlementDateChanged,
            onClose = { viewModel.onSettlementDateVisibilityChanged(false) },
        )
    }
    controller.registerBottomSheet<ExpensesSheets.Accounts> {
        ExpenseAccountsBottomSheet(
            active = state.activeAccounts,
            selectedId = state.account?.id,
            busy = state.busy,
            onSelect = viewModel::onAccountSelected,
            onManage = { viewModel.onManageAccountsVisibilityChanged(true) },
            onCreate = {
                viewModel.onAccountsVisibilityChanged(false)
                onCreateAccount()
            },
        )
    }
    controller.registerBottomSheet<ExpensesSheets.ManageAccounts> {
        ManageExpenseAccountsBottomSheet(
            accounts = state.activeAccounts + state.archivedAccounts,
            userId = state.userId,
            isOwner = state.isOwner,
            busy = state.busy,
            error = state.error,
            onSelect = viewModel::onAccountSelected,
            onEdit = { account ->
                viewModel.onManageAccountsVisibilityChanged(false)
                onEditAccount(account)
            },
            onArchive = viewModel::onArchiveAccount,
            onDelete = viewModel::onDeleteAccountRequested,
            onClose = { viewModel.onManageAccountsVisibilityChanged(false) },
            onDismissError = viewModel::onDismissError,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            if (state.showSettlementDate) viewModel.onSettlementDateVisibilityChanged(false)
            else if (state.showSettle) viewModel.onSettlementSheetVisibilityChanged(false)
            else if (state.showManageAccounts) viewModel.onManageAccountsVisibilityChanged(false)
            else viewModel.onAccountsVisibilityChanged(false)
        },
        content = {
            Box(Modifier.fillMaxSize()) {
                content()
                SuccessSnackbar(
                    message = state.successMessage,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        },
    )
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            confirmationTag = "confirm_delete_account",
            onConfirm = viewModel::onConfirmDeleteAccount,
            onDismiss = viewModel::onCancelDeleteAccount,
        )
    }
}
