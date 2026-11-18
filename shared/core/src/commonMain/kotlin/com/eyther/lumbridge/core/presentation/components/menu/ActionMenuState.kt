package com.eyther.lumbridge.core.presentation.components.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Transient popup visibility; the caller retains ownership of the actions and their results. */
@Stable
class ActionMenuState {
    var expanded: Boolean by mutableStateOf(false)
        private set

    fun show() {
        expanded = true
    }

    fun dismiss() {
        expanded = false
    }
}

@Composable fun rememberActionMenuState(): ActionMenuState = remember { ActionMenuState() }
