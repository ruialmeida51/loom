package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.sheet.ShoppingSheetHost
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.shopping.presentation.screen.ShoppingScreen
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ShoppingHost(portalToolbar: @Composable () -> Unit, onRootChanged: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { onRootChanged(true) }
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.SHOPPING)
    val viewModel: ShoppingScreenViewModelContract = koinViewModel()
    DisposableEffect(viewModel) { onDispose { viewModel.onDismissUndo() } }
    Scaffold(topBar = portalToolbar) { padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
            ShoppingSheetHost(viewModel = viewModel) { ShoppingScreen(viewModel = viewModel) }
        }
    }
}
