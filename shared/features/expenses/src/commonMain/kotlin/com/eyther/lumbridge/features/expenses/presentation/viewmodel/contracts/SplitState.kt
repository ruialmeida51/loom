package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.toImmutableSet

data class SplitState(
    val total: String,
    val currency: String = "EUR",
    val members: List<MemberDomain>,
    val method: ExpenseSplitMethodDomain = ExpenseSplitMethodDomain.EQUAL,
    val selected: Set<String> = emptySet(),
    val custom: Map<String, String> = emptyMap(),
    val amounts: Map<String, String> = emptyMap(),
    val selection: ExpenseSplitSelectionDomain? = null,
    val error: TextResource? = null,
    val confirmed: ExpenseSplitSelectionDomain? = null,
) {
    val canDecrease =
        selected
            .filter { id ->
                method.steppedInput(
                    value = custom[id].orEmpty(),
                    currency = currency,
                    increase = false,
                ) != custom[id].orEmpty()
            }
            .toImmutableSet()
    val canIncrease =
        selected
            .filter { id ->
                method.steppedInput(
                    value = custom[id].orEmpty(),
                    currency = currency,
                    increase = true,
                ) != custom[id].orEmpty()
            }
            .toImmutableSet()
}
