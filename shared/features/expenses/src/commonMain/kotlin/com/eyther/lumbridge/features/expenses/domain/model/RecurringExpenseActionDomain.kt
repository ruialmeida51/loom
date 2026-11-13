package com.eyther.lumbridge.features.expenses.domain.model

enum class RecurringExpenseActionDomain(val value: String) {
    PAUSE("pause"),
    RESUME("resume"),
    CANCEL("cancel"),
}
