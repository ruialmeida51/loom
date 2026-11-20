package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain

@Stable
data class SuggestedRepaymentUi(
    val settlement: SuggestedSettlementDomain,
    val fromName: String?,
    val toName: String?,
    val amount: String,
    val isCurrentUserSender: Boolean,
    val isCurrentUserRecipient: Boolean,
    val canRecord: Boolean = false,
    val recordEnabled: Boolean = canRecord,
) {
    val counterpartyId: String = if (isCurrentUserSender) settlement.to else settlement.from
    val counterpartyInitials: String =
        (if (isCurrentUserSender) toName else fromName).orEmpty().trim().take(1).uppercase()
}
