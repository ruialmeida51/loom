package com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import com.eyther.lumbridge.features.insights.presentation.model.SpendingCategoryUi
import kotlinx.coroutines.flow.take

data class InsightsState(
    val month: String = todayIso().take(7),
    val statement: MonthlyStatementDomain? = null,
    val busy: Boolean = false,
    val message: TextResource? = null,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError

    val categoryRows: List<SpendingCategoryUi> = statement?.let { current ->
        current.categories.entries.sortedByDescending { it.value }.map { (category, amount) ->
            SpendingCategoryUi(category = category, amountMinor = amount, totalMinor = current.totalMinor)
        }
    }.orEmpty()
}
