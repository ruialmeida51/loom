package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.sheet.AccountSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.account.presentation.components.bottomsheet.NicknameBottomSheet
import com.eyther.lumbridge.features.account.presentation.screen.AccountScreen
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun AccountHost(
    viewModel: AccountScreenViewModelContract,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.ACCOUNT)
    val controller = rememberBottomSheetController()
    val legalDocuments: GetLegalDocumentsUseCase = koinInject()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(state.showNickname) {
        if (state.showNickname) controller.showBottomSheet(AccountSheets.Nickname)
        else controller.hideBottomSheet()
    }
    controller.registerBottomSheet<AccountSheets.Nickname> {
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
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = { viewModel.onCloseNickname() },
    ) {
        FlowScaffold(title = stringResource(Res.string.portal_my_account), onBack = onBack) {
            AccountScreen(
                viewModel = viewModel,
                onSignOut = onSignOut,
                onOpenLegalDocument = { document ->
                    uriHandler.openUri(legalDocuments().url(document = document))
                },
            )
        }
    }
}
