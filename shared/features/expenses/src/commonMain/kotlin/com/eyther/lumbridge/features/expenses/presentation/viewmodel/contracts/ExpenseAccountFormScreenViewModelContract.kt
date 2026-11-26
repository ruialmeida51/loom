package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseAccountFormScreenViewModelContract : LoomViewModel() {
    sealed interface RedirectTo {
        /** The account was saved; it is now the selected one. */
        @Stable data class Saved(val accountId: String) : RedirectTo
    }

    data class Params(val accountId: String? = null)

    abstract val state: StateFlow<ExpenseAccountFormState>

    abstract fun onNameChanged(value: String)

    abstract fun onCurrencyChanged(value: String)

    abstract fun onIconChanged(value: ExpenseAccountIconDomain)

    abstract fun onParticipantToggled(id: String)

    /** Adds a guest: straight away when editing, with the account when creating it. */
    abstract fun onGuestAdded(name: String)

    abstract fun onGuestRenamed(id: String, name: String)

    abstract fun onGuestRemoved(id: String)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()

    abstract fun onNavigationHandled()
}
