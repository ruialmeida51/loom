package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseFormDomain
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetExpenseFormUseCase(
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val getExpenseUseCase: GetExpenseUseCase,
    private val householdDataSource: HouseholdDataSource,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(
        accountId: String,
        expenseId: String?,
        refreshReferences: Boolean = false,
    ): AppResult<ExpenseFormDomain> = safeCall {
        coroutineScope {
            val accountRequest = async {
                if (refreshReferences)
                    expenseAccountDataSource.getAccount(id = accountId).getOrThrow()
                else expenseAccountDataSource.getReferenceAccount(id = accountId).getOrThrow()
            }
            val membersRequest = async {
                if (refreshReferences)
                    householdDataSource.getMembers(householdId = context.id).getOrThrow()
                else householdDataSource.getMemberReferences(householdId = context.id).getOrThrow()
            }
            val expenseRequest = async {
                expenseId?.let { getExpenseUseCase(id = it, accountId = accountId).getOrThrow() }
            }
            val account = accountRequest.await()
            ExpenseFormDomain(
                account = account,
                members = membersRequest.await().filter { it.id in account.memberIds },
                expense = expenseRequest.await(),
            )
        }
    }
}
