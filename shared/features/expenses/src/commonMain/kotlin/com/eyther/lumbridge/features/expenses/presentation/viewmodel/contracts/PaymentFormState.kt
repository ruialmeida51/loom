package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

data class PaymentFormState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val payment: SettlementDomain? = null,
    val members: ImmutableList<MemberDomain> = persistentListOf(),
    val from: String = "",
    val to: String = "",
    val amount: String = "",
    val occurredOn: String = "",
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val saved: Boolean = false,
) {
    val currency: String = account?.currency ?: "EUR"
    val date: LocalDate? = occurredOn.takeIf { it.isNotEmpty() }?.let(LocalDate::parse)
    val canSave: Boolean =
        !loading &&
            !busy &&
            !saved &&
            account?.canAddExpense(userId = userId) == true &&
            from.isNotBlank() &&
            to.isNotBlank() &&
            from != to &&
            amount.isNotBlank() &&
            (payment == null ||
                payment.canManage(
                    userId = userId,
                    isOwner = members.any { it.id == userId && it.role == "owner" },
                ))
}
