package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.eyther.lumbridge.app.navigation.model.sheet.HouseholdDetailsSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.expenses.presentation.screen.MemberAccountPositionsScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.MemberAccountPositionsScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.components.bottomsheet.HouseholdConfirmationBottomSheet
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun HouseholdDetailsSheetHost(
    viewModel: HouseholdDetailsScreenViewModelContract,
    onExited: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    LaunchedEffect(state.exited) { if (state.exited) onExited(state.exitExplicit) }
    LaunchedEffect(state.confirmation?.requestId) {
        if (state.confirmation != null)
            controller.showBottomSheet(HouseholdDetailsSheets.Confirmation)
        else controller.hideBottomSheet()
    }
    controller.registerBottomSheet<HouseholdDetailsSheets.Confirmation> {
        val confirmation = state.confirmationContent
        val target = state.confirmation
        if (confirmation != null && target != null)
            key(target.requestId) {
                val memberId = confirmation.balanceMemberId
                val balanceOwner = rememberHostViewModelStoreOwner()
                val balances: MemberAccountPositionsScreenViewModelContract? =
                    if (memberId != null)
                        koinViewModel(viewModelStoreOwner = balanceOwner) {
                            parametersOf(
                                MemberAccountPositionsScreenViewModelContract.Params(
                                    memberId = memberId,
                                ),
                            )
                        }
                    else null
                val balancesState = balances?.state?.platformCollectAsStateOnStart()?.value
                HouseholdConfirmationBottomSheet(
                    confirmation = confirmation,
                    name = state.householdName,
                    confirmationName = state.confirmationName,
                    busy = state.busy,
                    canConfirm =
                        state.canConfirm && (memberId == null || balancesState?.ready == true),
                    error = state.confirmationError,
                    onNameChanged = viewModel::onConfirmationNameChanged,
                    onConfirm = viewModel::onConfirm,
                    onDismiss = viewModel::onConfirmationDismissed,
                    balanceContent = {
                        balances?.let { MemberAccountPositionsScreen(viewModel = it) }
                    },
                )
            }
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = viewModel::onConfirmationDismissed,
        content = content,
    )
}
