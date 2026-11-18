package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain

data class NotificationsPrimeState(
    val visible: Boolean = false,
    val busy: Boolean = false,
    val permission: NotificationPermissionDomain = NotificationPermissionDomain.NOT_REQUESTED,
    val waitingForSettings: Boolean = false,
    val error: TextResource? = null,
)
