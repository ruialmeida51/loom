package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.getOrThrow
import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.datetime.LocalDate

class AddExpenseUseCase(
    private val source: ExpenseDataSource,
    private val members: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(expense: ExpenseDomain): AppResult<Unit> = safeCall {
        require(expense.title.trim().length in 1..160) {
            "expenses.message_give_the_expense_a_title_up_to_160_characters"
        }
        val date =
            try {
                LocalDate.parse(expense.occurredOn)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("expenses.message_invalid_payment_date")
            }
        require(date <= LocalDate.parse(todayIso(context.timeZone))) {
            "expenses.message_payment_date_future"
        }
        ExpenseSplitDomain.validate(expense.amountMinor, expense.shares)
        val memberIds = members(context.id).getOrThrow().map { it.id }.toSet()
        require(expense.paidBy in memberIds && memberIds.containsAll(expense.shares.keys)) {
            "expenses.message_choose_current_members_of_this_household"
        }
        source.add(expense.copy(title = expense.title.trim())).getOrThrow()
    }
}
