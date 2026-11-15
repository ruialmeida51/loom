package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.sheet.ShoppingSheetHost
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.shopping.presentation.screen.ShoppingScreen
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.action_undo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ShoppingHost(portalToolbar: @Composable () -> Unit, onRootChanged: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { onRootChanged(true) }
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.SHOPPING)
    val viewModel: ShoppingScreenViewModelContract = koinViewModel()
    val state by viewModel.state.platformCollectAsStateOnStart()
    DisposableEffect(viewModel) { onDispose { viewModel.onDismissUndo() } }
    val snackbarHost = remember { SnackbarHostState() }
    val undoLabel = stringResource(Res.string.action_undo)
    val undoMessage = state.undoMessage?.getString()
    LaunchedEffect(undoMessage) {
        val message = undoMessage
        if (message != null) {
            val result = snackbarHost.showSnackbar(message = message, actionLabel = undoLabel)
            if (result == SnackbarResult.ActionPerformed) viewModel.onUndo()
            else viewModel.onDismissUndo()
        }
    }
    Scaffold(topBar = portalToolbar, snackbarHost = { SnackbarHost(hostState = snackbarHost) }) {
        padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
            ShoppingSheetHost(viewModel = viewModel) { ShoppingScreen(viewModel = viewModel) }
        }
    }
}
