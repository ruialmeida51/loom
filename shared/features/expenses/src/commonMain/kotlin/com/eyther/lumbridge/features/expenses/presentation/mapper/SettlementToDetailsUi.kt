package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.model.PaymentDetailsUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.datetime.LocalDate

fun SettlementDomain.toDetailsUi(
    account: ExpenseAccountDomain,
    members: List<MemberDomain>,
    userId: String,
): PaymentDetailsUi =
    PaymentDetailsUi(
        amount = MoneyDomain(minorUnits = amountMinor, currency = account.currency).formatted(),
        fromName = members.firstOrNull { it.id == from }?.name,
        toName = members.firstOrNull { it.id == to }?.name,
        isUserSender = from == userId,
        isUserRecipient = to == userId,
        occurredOn = LocalDate.parse(occurredOn),
        accountName = account.name,
        isEveryday = account.isDefault,
    )
