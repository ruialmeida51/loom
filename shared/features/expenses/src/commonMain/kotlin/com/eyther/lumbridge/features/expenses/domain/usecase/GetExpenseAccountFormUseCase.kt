package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountFormDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase

class GetExpenseAccountFormUseCase(
    private val getExpenseAccountUseCase: GetExpenseAccountUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(id: String?): AppResult<ExpenseAccountFormDomain> = safeCall {
        val account = id?.let { getExpenseAccountUseCase(id = it).getOrThrow() }
        ExpenseAccountFormDomain(
            account = account,
            members =
                getMembersUseCase(id = context.id, includeInactive = id != null)
                    .getOrThrow()
                    .filter { it.active || it.id in account?.memberIds.orEmpty() },
        )
    }
}
