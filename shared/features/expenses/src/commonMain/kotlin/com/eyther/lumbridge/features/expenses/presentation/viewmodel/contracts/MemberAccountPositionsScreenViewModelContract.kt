package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class MemberAccountPositionsScreenViewModelContract : LoomViewModel() {
    data class Params(val memberId: String)

    abstract val state: StateFlow<MemberAccountPositionsState>

    abstract fun onRetry()
}
