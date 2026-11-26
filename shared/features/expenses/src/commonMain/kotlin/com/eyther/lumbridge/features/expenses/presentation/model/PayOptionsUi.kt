package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain

/** How to send a repayment to [recipientName]; [amount] is null until a valid one is typed. */
@Immutable
data class PayOptionsUi(
    val recipientName: String,
    val handles: PaymentHandlesDomain,
    val amount: String?,
    val currency: String,
) {
    val paypalLink: String? = amount?.let { handles.paypalLink(amount = it, currency = currency) }
    val revolutLink: String? = handles.revolutLink()

    /** The amount as people read it, such as "€20.22"; the links keep [amount]. */
    val amountLabel: String =
        amount
            ?.let { runCatching { MoneyDomain.parse(text = it, currency = currency).formatted() }.getOrNull() }
            .orEmpty()
}
