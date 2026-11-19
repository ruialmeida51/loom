package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.presentation.model.MemberAccountPositionUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MemberAccountPositionsState(
    val positions: ImmutableList<MemberAccountPositionUi> = persistentListOf(),
    val loading: Boolean = true,
    val error: TextResource? = null,
) {
    val ready: Boolean = !loading && error == null
}
