package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.HouseholdDetailsSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.household.presentation.components.bottomsheet.InvitationBottomSheet
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract

@Composable
internal fun HouseholdDetailsSheetHost(
    viewModel: HouseholdDetailsScreenViewModelContract,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    LaunchedEffect(state.invitation) {
        if (state.invitation != null) controller.showBottomSheet(HouseholdDetailsSheets.Invitation)
        else controller.hideBottomSheet()
    }
    controller.registerBottomSheet<HouseholdDetailsSheets.Invitation> {
        InvitationBottomSheet(
            invitation = state.invitation.orEmpty(),
            onClose = viewModel::onCloseInvitation,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            viewModel.onCloseInvitation()
        },
        content = content,
    )
}
