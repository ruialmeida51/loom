package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.SettingsSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.settings.presentation.components.bottomsheet.InvitationBottomSheet
import com.eyther.lumbridge.features.settings.presentation.components.bottomsheet.NicknameBottomSheet
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract

@Composable
internal fun SettingsSheetHost(
    viewModel: SettingsScreenViewModelContract,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showNickname -> SettingsSheets.Nickname
            state.showInvitation -> SettingsSheets.Invitation
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<SettingsSheets.Nickname> {
        NicknameBottomSheet(
            nickname = state.nickname,
            error = state.nicknameError,
            busy = state.busy,
            canSave = state.canSaveNickname,
            onChanged = viewModel::onNicknameChanged,
            onSave = viewModel::onSaveNickname,
            onClose = viewModel::onCloseNickname,
        )
    }
    controller.registerBottomSheet<SettingsSheets.Invitation> {
        InvitationBottomSheet(
            invitation = state.invitation.orEmpty(),
            onClose = viewModel::onCloseInvitation,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            if (state.showNickname) viewModel.onCloseNickname() else viewModel.onCloseInvitation()
        },
        content = content,
    )
}
