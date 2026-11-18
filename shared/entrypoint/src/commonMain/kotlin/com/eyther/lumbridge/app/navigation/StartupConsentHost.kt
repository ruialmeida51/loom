package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.ConsentSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.viewmodel.rememberHostViewModelStoreOwner
import com.eyther.lumbridge.features.settings.presentation.screen.UsageDataConsentScreen
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentScreenViewModelContract
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StartupConsentHost(content: @Composable () -> Unit) {
    val sheetViewModelOwner = rememberHostViewModelStoreOwner()
    val viewModel: UsageDataConsentScreenViewModelContract =
        koinViewModel(viewModelStoreOwner = sheetViewModelOwner)
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    LaunchedEffect(viewModel) { viewModel.onStartup() }
    LaunchedEffect(state.visible, state.busy, state.error) {
        if (state.visible) controller.showBottomSheet(ConsentSheets.UsageData)
        else controller.hideBottomSheet()
    }
    controller.registerBottomSheet<ConsentSheets.UsageData> {
        UsageDataConsentScreen(viewModel = viewModel)
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = viewModel::onDismiss,
        content = content,
    )
}
