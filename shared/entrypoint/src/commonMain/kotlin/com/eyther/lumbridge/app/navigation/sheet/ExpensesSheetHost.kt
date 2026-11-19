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
            state.showAccounts -> ExpensesSheets.Accounts
            else -> null
        }
    LaunchedEffect(requested) {
        if (requested == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requested)
    }
    controller.registerBottomSheet<ExpensesSheets.Accounts> {
        ExpenseAccountsBottomSheet(
            active = state.activeAccounts,
            archived = state.archivedAccounts,
            manageableAccountIds = state.manageableAccountIds,
            selectedId = state.account?.id,
            busy = state.busy,
            error = state.error,
            onSelect = viewModel::onAccountSelected,
            onCreate = {
                viewModel.onAccountsVisibilityChanged(false)
                onCreateAccount()
            },
            onEdit = { account ->
                viewModel.onAccountsVisibilityChanged(false)
                onEditAccount(account)
            },
            onArchive = viewModel::onArchiveAccount,
            onDelete = viewModel::onDeleteAccountRequested,
            onDismissError = viewModel::onDismissError,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            viewModel.onAccountsVisibilityChanged(false)
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
            error = state.error,
            onConfirm = viewModel::onConfirmDeleteAccount,
            onDismiss = viewModel::onCancelDeleteAccount,
        )
    }
}
