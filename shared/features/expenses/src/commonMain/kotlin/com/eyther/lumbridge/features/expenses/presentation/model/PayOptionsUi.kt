package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain

/**
 * How to send money to [recipientName]; [amount] is null until a valid one is typed. Without an
 * amount PayPal is offered only when [linkWithoutAmount] (a profile, where there is no amount to
 * wait for), opening their PayPal.me page instead.
 */
@Immutable
data class PayOptionsUi(
    val recipientName: String,
    val handles: PaymentHandlesDomain,
    val amount: String?,
    val currency: String,
    val linkWithoutAmount: Boolean = false,
) {
    val paypalLink: String? =
        amount?.let { handles.paypalLink(amount = it, currency = currency) }
            ?: handles.paypalProfileLink()?.takeIf { linkWithoutAmount }
    val revolutLink: String? = handles.revolutLink()

    /** The amount as people read it, such as "€20.22"; the links keep [amount]. */
    val amountLabel: String =
        amount
            ?.let { runCatching { MoneyDomain.parse(text = it, currency = currency).formatted() }.getOrNull() }
            .orEmpty()
}
