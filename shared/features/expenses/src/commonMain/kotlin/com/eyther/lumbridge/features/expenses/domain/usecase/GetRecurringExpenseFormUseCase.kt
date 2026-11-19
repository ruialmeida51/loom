package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseFormDomain
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetRecurringExpenseFormUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val householdDataSource: HouseholdDataSource,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(
        id: String,
        accountId: String,
    ): AppResult<RecurringExpenseFormDomain> = safeCall {
        coroutineScope {
            val ruleRequest = async {
                recurringExpenseDataSource.getRule(id = id, accountId = accountId).getOrThrow()
            }
            val accountRequest = async {
                expenseAccountDataSource.getAccount(id = accountId).getOrThrow()
            }
            val membersRequest = async {
                householdDataSource.getMemberHistory(householdId = context.id).getOrThrow()
            }
            val rule = ruleRequest.await()
            val account = accountRequest.await()
            val members = membersRequest.await()
            RecurringExpenseFormDomain(
                rule = rule,
                account = account,
                actorIsOwner = members.any { it.id == context.userId && it.isOwner },
                members =
                    members.filter {
                        (it.active && it.id in account.memberIds) ||
                            it.id == rule.paidBy ||
                            it.id in rule.shares
                    },
            )
        }
    }
}
