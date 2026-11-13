package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.*

data class SettingsState(
    val preferences: SettingsDomain = SettingsDomain(),
    val members: List<MemberDomain> = emptyList(),
    val invitation: String? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class SettingsScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<SettingsState>

    abstract fun theme(value: String)

    abstract fun notifications(enabled: Boolean)

    abstract fun invite()

    abstract fun dismiss()
}
