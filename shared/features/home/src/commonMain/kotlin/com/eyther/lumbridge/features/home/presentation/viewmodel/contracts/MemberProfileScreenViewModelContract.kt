package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class MemberProfileScreenViewModelContract : LoomViewModel() {
    data class Params(val memberId: String)

    abstract val state: StateFlow<MemberProfileState>

    abstract fun onRetry()
}
