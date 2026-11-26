package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.model.PayOptionsUi
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
    val occurredOn: LocalDate? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: PaymentFormScreenViewModelContract.RedirectTo? = null,
    val paymentHandles: Map<String, PaymentHandlesDomain> = emptyMap(),
) {
    val currency: String = account?.currency ?: "EUR"

    /**
     * Ways to send this payment, when the person paying is the one recording it and the recipient
     * shares how they like to be paid. The amount is in major units, ready for a payment link.
     */
    val payOptions: PayOptionsUi? =
        paymentHandles[to]
            ?.takeIf { payment == null && from == userId && to != userId && !it.isEmpty }
            ?.let { handles ->
                PayOptionsUi(
                    recipientName = members.firstOrNull { it.id == to }?.name.orEmpty(),
                    handles = handles,
                    amount =
                        runCatching { MoneyDomain.parse(text = amount, currency = currency) }
                            .getOrNull()
                            ?.takeIf { it.minorUnits > 0 }
                            ?.inputText(),
                    currency = currency,
                )
            }
    val date: LocalDate? = occurredOn
    val canSave: Boolean =
        !loading &&
            !busy &&
            redirectTo == null &&
            account?.canAddExpense(userId = userId) == true &&
            from.isNotBlank() &&
            to.isNotBlank() &&
            from != to &&
            amount.isNotBlank() &&
            (payment == null && account.canRecordPayment(userId, from, to) ||
                payment != null &&
                payment.canManage(
                    userId = userId,
                    isOwner = members.any { it.id == userId && it.role == "owner" },
                ))
}
