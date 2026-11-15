package com.eyther.lumbridge.app.navigation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar

@Composable
internal fun FlowScaffold(
    title: String,
    onBack: () -> Unit,
    backEnabled: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            Toolbar(
                text = title,
                showBackButton = true,
                onBack = onBack,
                backEnabled = backEnabled,
                actions = actions,
            )
        },
    ) { padding ->
        Box(
            Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize(),
        ) {
            content()
        }
    }
}
