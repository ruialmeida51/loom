package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain

data class SessionState(
    val stage: SessionStageUi = SessionStageUi.RESTORING,
    val userId: String? = null,
    val context: HouseholdContextDomain? = null,
    val reconnecting: Boolean = false,
    val sessionRecoveryAvailable: Boolean = false,
    val recoveringSession: Boolean = false,
    val sessionRecoveryError: TextResource? = null,
    val restoreError: TextResource? = null,
    val selectionError: TextResource? = null,
    val accessError: TextResource? = null,
    val selectionMessage: TextResource? = null,
    val signingOut: Boolean = false,
    val signOutError: TextResource? = null,
)
