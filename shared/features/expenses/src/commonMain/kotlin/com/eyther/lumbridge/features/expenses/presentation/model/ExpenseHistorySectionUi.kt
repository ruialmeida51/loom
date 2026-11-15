package com.eyther.lumbridge.features.expenses.presentation.model

import kotlinx.collections.immutable.ImmutableList

data class ExpenseHistorySectionUi(val date: String, val items: ImmutableList<ExpenseHistoryItemUi>)
