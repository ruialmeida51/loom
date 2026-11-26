package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain

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
}
