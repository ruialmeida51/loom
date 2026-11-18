package com.eyther.lumbridge.features.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.settings.presentation.components.bottomsheet.UsageDataConsentBottomSheet
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentScreenViewModelContract

@Composable
fun UsageDataConsentScreen(viewModel: UsageDataConsentScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    UsageDataConsentBottomSheet(
        analytics = state.analytics,
        crashReports = state.crashReports,
        analyticsAvailable = state.preferences.analyticsAvailable,
        crashReportsAvailable = state.preferences.crashReportsAvailable,
        ready = state.preferences.initialized,
        busy = state.busy,
        error = state.error,
        onAnalytics = viewModel::onAnalyticsChanged,
        onCrashReports = viewModel::onCrashReportsChanged,
        onAllowAll = viewModel::onAllowAll,
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
    )
}
