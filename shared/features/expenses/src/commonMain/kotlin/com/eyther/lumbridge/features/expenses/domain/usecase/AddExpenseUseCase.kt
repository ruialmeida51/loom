package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.datetime.LocalDate

class AddExpenseUseCase(
    private val expenseDataSource: ExpenseDataSource,
    private val members: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {

    suspend operator fun invoke(expense: ExpenseDomain): AppResult<Unit> = safeCall {
        requireDomain(expense.title.trim().length in 1..160) {
            ExpenseError.InvalidTitle()
        }
        val date =
            try {
                LocalDate.parse(expense.occurredOn)
            } catch (_: IllegalArgumentException) {
                throw ExpenseError.InvalidPaymentDate()
            }
        requireDomain(date <= LocalDate.parse(todayIso(context.timeZone))) {
            ExpenseError.FuturePaymentDate()
        }
        ExpenseSplitDomain.validate(expense.amountMinor, expense.shares)
        val memberIds = members(context.id).getOrThrow().map { it.id }.toSet()
        requireDomain(expense.paidBy in memberIds && memberIds.containsAll(expense.shares.keys)) {
            ExpenseError.InvalidMembers()
        }
        expenseDataSource.addExpense(expense.copy(title = expense.title.trim())).getOrThrow()
    }
}
