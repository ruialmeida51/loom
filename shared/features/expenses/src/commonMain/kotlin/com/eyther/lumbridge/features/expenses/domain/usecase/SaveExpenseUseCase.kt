package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import com.eyther.lumbridge.features.expenses.domain.model.TravelCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

class SaveExpenseUseCase(
    private val expenseDataSource: ExpenseDataSource,
    private val expenseAccountDataSource: ExpenseAccountDataSource,
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
        coroutineScope {
            val memberRequest = async {
                members(id = context.id, includeInactive = expense.version > 0)
                    .getOrThrow()
                    .map { it.id }
                    .toSet()
            }
            val accountRequest = async {
                expenseAccountDataSource.getAccount(id = expense.accountId).getOrThrow()
            }
            val account = accountRequest.await()
            requireDomain(account.canAddExpense(userId = context.userId)) {
                ExpenseAccountError.Archived()
            }
            expense.original?.let { original ->
                requireDomain(!expense.recurring) { ExpenseError.RecurringSingleCurrency() }
                requireDomain(
                    original.currency != account.currency &&
                        TravelCurrencyDomain.isSupported(original.currency) &&
                        OriginalAmountDomain.normalizeRate(original.rate) == original.rate &&
                        original.convertedMinor() == expense.amountMinor,
                ) {
                    ExpenseError.InvalidConversion()
                }
            }
            // Guests take part like members, inside their own account only.
            val people =
                memberRequest.await() +
                    account.guests
                        .filter { !it.removed || expense.version > 0 }
                        .map { it.id }
            requireDomain(expense.paidBy in people && people.containsAll(expense.shares.keys)) {
                ExpenseError.InvalidMembers()
            }
            requireDomain(
                expense.paidBy in account.participantIds &&
                    account.participantIds.containsAll(expense.shares.keys),
            ) {
                ExpenseAccountError.InvalidMembers()
            }
            expenseDataSource.saveExpense(expense.copy(title = expense.title.trim())).getOrThrow()
        }
    }
}
