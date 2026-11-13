package com.eyther.lumbridge.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.eyther.lumbridge.app.LoomApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Loom",
        state = rememberWindowState(width = 440.dp, height = 920.dp),
    ) {
        LoomApp()
    }
}
