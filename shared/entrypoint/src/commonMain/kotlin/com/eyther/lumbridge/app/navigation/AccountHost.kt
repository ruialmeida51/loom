package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.sheet.AccountSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.account.presentation.components.bottomsheet.NicknameBottomSheet
import com.eyther.lumbridge.features.account.presentation.screen.AccountScreen
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import com.eyther.lumbridge.features.settings.presentation.screen.NotificationsPrimeScreen
import com.eyther.lumbridge.features.settings.presentation.screen.UsageDataConsentScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeScreenViewModelContract
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AccountHost(
    viewModel: AccountScreenViewModelContract,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    signingOut: Boolean = false,
    signOutError: TextResource? = null,
    onSignOutDismissed: () -> Unit = {},
) {
    val sheetViewModelOwner = rememberHostViewModelStoreOwner()
    val state by viewModel.state.platformCollectAsStateOnStart()
    val consentViewModel: UsageDataConsentScreenViewModelContract =
        koinViewModel(viewModelStoreOwner = sheetViewModelOwner)
    val consentState by consentViewModel.state.platformCollectAsStateOnStart()
    val notificationViewModel: NotificationsPrimeScreenViewModelContract =
        koinViewModel(viewModelStoreOwner = sheetViewModelOwner)
    val notificationState by notificationViewModel.state.platformCollectAsStateOnStart()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { notificationViewModel.onResume() }
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.ACCOUNT)
    val controller = rememberBottomSheetController()
    val legalDocuments: GetLegalDocumentsUseCase = koinInject()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(
        state.showNickname,
        consentState.visible,
        consentState.busy,
        consentState.error,
        notificationState.visible,
        notificationState.busy,
        notificationState.error,
    ) {
        if (consentState.visible) controller.showBottomSheet(AccountSheets.UsageData)
        else if (notificationState.visible) controller.showBottomSheet(AccountSheets.Notifications)
        else if (state.showNickname) controller.showBottomSheet(AccountSheets.Nickname)
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
    controller.registerBottomSheet<AccountSheets.UsageData> {
        UsageDataConsentScreen(viewModel = consentViewModel)
    }
    controller.registerBottomSheet<AccountSheets.Notifications> {
        NotificationsPrimeScreen(viewModel = notificationViewModel)
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled =
            !state.busy && !signingOut && !consentState.busy && !notificationState.busy,
        onDismissed = {
            if (consentState.visible) consentViewModel.onDismiss()
            else if (notificationState.visible) notificationViewModel.onNotNow()
            else viewModel.onCloseNickname()
        },
    ) {
        FlowScaffold(
            title = stringResource(Res.string.portal_my_account),
            onBack = onBack,
            backEnabled = !signingOut,
        ) {
            AccountScreen(
                viewModel = viewModel,
                onSignOut = onSignOut,
                signingOut = signingOut,
                signOutError = signOutError,
                onSignOutDismissed = onSignOutDismissed,
                onUsageData = consentViewModel::onOpen,
                onAllowNotifications = notificationViewModel::onOpen,
                onOpenLegalDocument = { document ->
                    uriHandler.openUri(legalDocuments().url(document = document))
                },
            )
        }
    }
    BusyNavigationGuard(busy = signingOut)
}
