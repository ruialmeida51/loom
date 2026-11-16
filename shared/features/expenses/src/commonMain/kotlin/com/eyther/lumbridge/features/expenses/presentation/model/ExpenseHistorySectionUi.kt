package com.eyther.lumbridge.features.expenses.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class ExpenseHistorySectionUi(
    val date: String,
    val items: ImmutableList<ExpenseHistoryItemUi>,
) {
    val calendarDate: LocalDate = LocalDate.parse(date)
}
