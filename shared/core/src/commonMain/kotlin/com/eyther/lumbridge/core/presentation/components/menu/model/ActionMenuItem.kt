package com.eyther.lumbridge.core.presentation.components.menu.model

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class ActionMenuItem(
    val text: TextResource,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val destructive: Boolean = false,
)
