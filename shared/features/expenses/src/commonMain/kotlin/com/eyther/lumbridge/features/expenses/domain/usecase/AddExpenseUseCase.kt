package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.datetime.LocalDate

class AddExpenseUseCase(
    private val source: ExpenseDataSource,
    private val members: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(expense: ExpenseDomain): AppResult<Unit> = safeCall {
        require(expense.title.trim().length in 1..160) {
            "Give the expense a title, up to 160 characters."
        }
        LocalDate.parse(expense.occurredOn)
        ExpenseSplitDomain.validate(expense.amountMinor, expense.shares)
        val memberIds = members(context.id).getOrThrow().map { it.id }.toSet()
        require(expense.paidBy in memberIds && memberIds.containsAll(expense.shares.keys)) {
            "Choose current members of this household."
        }
        source.add(expense.copy(title = expense.title.trim())).getOrThrow()
    }
}
