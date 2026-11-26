package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseAccountFormState(
    val userId: String,
    val name: String = "",
    val currency: String = "EUR",
    val icon: ExpenseAccountIconDomain = ExpenseAccountIconDomain.TRAVEL,
    val members: List<MemberDomain> = emptyList(),
    val selected: Set<String> = emptySet(),
    /** Guests still on the account: saved ones when editing, the draft when creating. */
    val guests: List<ExpenseGuestDomain> = emptyList(),
    val guestsEnabled: Boolean = true,
    /** The guest whose change is being saved, while it is. */
    val guestBusyId: String? = null,
    val account: ExpenseAccountDomain? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: ExpenseAccountFormScreenViewModelContract.RedirectTo? = null,
) {
    val canSave: Boolean = !loading && !busy && name.isNotBlank() && userId in selected

    /** Everyday never takes guests; new accounts are always trips or events. */
    val showsGuests: Boolean =
        !loading && (account?.acceptsGuests ?: true) && (guestsEnabled || guests.isNotEmpty())

    val canAddGuest: Boolean =
        guestsEnabled &&
            !busy &&
            guestBusyId == null &&
            account?.archived != true &&
            guests.size < ExpenseGuestDomain.MAXIMUM_PER_ACCOUNT
}
