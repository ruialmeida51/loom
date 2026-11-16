package com.eyther.lumbridge.core.presentation.components.container

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Plannot's refresh container adapted to host-owned toolbars/insets. The child owns scrolling and
 * content padding; the ViewModel owns refresh progress while loaded sections remain visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshContentContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = snackbarHost,
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize().testTag("pull_to_refresh"),
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            },
            content = content,
        )
    }
}
