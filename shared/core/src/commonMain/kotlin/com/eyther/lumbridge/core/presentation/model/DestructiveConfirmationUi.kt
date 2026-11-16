package com.eyther.lumbridge.core.presentation.model

import com.eyther.lumbridge.core.presentation.resources.TextResource

/** Feature-owned resource choices rendered by the generic permanent-action confirmation. */
data class DestructiveConfirmationUi(
    val title: TextResource,
    val message: TextResource,
    val confirmText: TextResource,
    val cancelText: TextResource,
)
