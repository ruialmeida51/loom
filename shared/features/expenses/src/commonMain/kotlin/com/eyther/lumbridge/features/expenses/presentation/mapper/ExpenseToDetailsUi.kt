package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseShareUi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

internal fun ExpenseDomain.toDetailsUi(
    account: ExpenseAccountDomain,
    userId: String,
    memberNames: Map<String, String>,
    memberPictures: Map<String, String?> = emptyMap(),
): ExpenseDetailsUi =
    ExpenseDetailsUi(
        title = title,
        amount = MoneyDomain(minorUnits = amountMinor, currency = account.currency).formatted(),
        category = category,
        occurredOn = LocalDate.parse(occurredOn),
        payerName = memberNames[paidBy],
        accountName = account.name,
        isEveryday = account.isDefault,
        recurring = recurring,
        originalAmount =
            original?.let { MoneyDomain(minorUnits = it.amountMinor, currency = it.currency).formatted() },
        originalRate = original?.let { "1 ${it.currency} = ${it.rate} ${account.currency}" },
        splitMethod = splitMethod,
        ownShare =
            shares[userId]?.let {
                MoneyDomain(minorUnits = it, currency = account.currency).formatted()
            },
        shares =
            shares
                .map { (id, amount) ->
                    val name = memberNames[id]
                    ExpenseShareUi(
                        id = id,
                        name = name,
                        initials =
                            name
                                ?.firstOrNull { it.isLetterOrDigit() }
                                ?.uppercaseChar()
                                ?.toString()
                                .orEmpty(),
                        amount =
                            MoneyDomain(minorUnits = amount, currency = account.currency)
                                .formatted(),
                        isCurrentUser = id == userId,
                        avatarPath = memberPictures[id],
                        isGuest = account.guests.any { it.id == id },
                        isPayer = id == paidBy,
                    )
                }
                .sortedWith(
                    compareByDescending<ExpenseShareUi> { it.isCurrentUser }
                        .thenBy { it.name?.lowercase() }
                        .thenBy { it.id },
                )
                .toImmutableList(),
    )
