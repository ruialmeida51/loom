package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain

internal fun ExpenseSplitMethodDomain.toWireValue(): String =
    when (this) {
        ExpenseSplitMethodDomain.EQUAL -> "equal"
        ExpenseSplitMethodDomain.SHARES -> "shares"
        ExpenseSplitMethodDomain.AMOUNTS -> "amounts"
        ExpenseSplitMethodDomain.PERCENTAGES -> "percentages"
    }

internal fun String.toSplitMethod(): ExpenseSplitMethodDomain =
    when (this) {
        "equal" -> ExpenseSplitMethodDomain.EQUAL
        "shares" -> ExpenseSplitMethodDomain.SHARES
        "percentages" -> ExpenseSplitMethodDomain.PERCENTAGES
        else -> ExpenseSplitMethodDomain.AMOUNTS
    }
