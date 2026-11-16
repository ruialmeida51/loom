package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseSplitViewModelContract : LoomViewModel() {
    data class Params(
        val amountMinor: Long,
        val currency: String,
        val members: List<MemberDomain>,
        val selection: ExpenseSplitSelectionDomain,
    )

    abstract val state: StateFlow<SplitState>

    abstract fun onMethodChanged(method: ExpenseSplitMethodDomain)

    abstract fun onParticipantToggled(id: String)

    abstract fun onCustomChanged(id: String, value: String)

    abstract fun onCustomStepped(id: String, increase: Boolean)

    abstract fun onConfirm()

    abstract fun onNavigationHandled()
}
