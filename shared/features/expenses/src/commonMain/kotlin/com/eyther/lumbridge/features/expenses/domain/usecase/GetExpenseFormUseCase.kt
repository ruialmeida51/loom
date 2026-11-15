package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseFormDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase

class GetExpenseFormUseCase(
    private val getExpenseAccountUseCase: GetExpenseAccountUseCase,
    private val getExpenseUseCase: GetExpenseUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(
        accountId: String,
        expenseId: String?,
    ): AppResult<ExpenseFormDomain> = safeCall {
        val account = getExpenseAccountUseCase(id = accountId).getOrThrow()
        val members =
            getMembersUseCase(context.id).getOrThrow().filter { it.id in account.memberIds }
        val expense = expenseId?.let {
            getExpenseUseCase(id = it, accountId = accountId).getOrThrow()
        }
        ExpenseFormDomain(account = account, members = members, expense = expense)
    }
}
