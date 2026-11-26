package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class SettlementFormDomain(
    val account: ExpenseAccountDomain,
    val members: List<MemberDomain>,
    val settlement: SettlementDomain?,
    /** How each participant likes to be paid, for those who share it. */
    val paymentHandles: Map<String, PaymentHandlesDomain> = emptyMap(),
)
