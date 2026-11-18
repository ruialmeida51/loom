package com.eyther.lumbridge.features.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.settings.presentation.components.bottomsheet.NotificationsPrimeBottomSheet
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeScreenViewModelContract

@Composable
fun NotificationsPrimeScreen(viewModel: NotificationsPrimeScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    NotificationsPrimeBottomSheet(
        permission = state.permission,
        busy = state.busy,
        error = state.error,
        onEnable = viewModel::onEnable,
        onNotNow = viewModel::onNotNow,
    )
}
