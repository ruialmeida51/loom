package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.datetime.LocalDate

/** Schedule edits affect future occurrences; active/paused state is never changed by Save. */
class SaveRecurringExpenseUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
) {
    suspend operator fun invoke(rule: RecurringExpenseDomain): AppResult<RecurringExpenseDomain> =
        safeCall {
            requireDomain(!rule.cancelled) { RecurringExpenseError.Cancelled() }
            requireDomain(rule.version > 0) { RecurringExpenseError.Changed() }
            requireDomain(rule.title.trim().length in 1..MAX_TITLE_LENGTH) {
                ExpenseError.InvalidTitle()
            }
            ExpenseSplitDomain.validate(amountMinor = rule.amountMinor, shares = rule.shares)
            recurringExpenseDataSource
                .save(rule = rule.copy(title = rule.title.trim()))
                .getOrThrow()
        }

    private companion object {
        const val MAX_TITLE_LENGTH = 160
    }
}
