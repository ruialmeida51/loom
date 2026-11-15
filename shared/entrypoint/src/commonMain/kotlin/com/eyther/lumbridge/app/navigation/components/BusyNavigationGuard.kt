package com.eyther.lumbridge.app.navigation.components

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/** Keep a submitted write's route alive until the server returns an authoritative result. */
@Composable
internal fun BusyNavigationGuard(busy: Boolean) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = busy,
        onBackCompleted = {},
    )
}
