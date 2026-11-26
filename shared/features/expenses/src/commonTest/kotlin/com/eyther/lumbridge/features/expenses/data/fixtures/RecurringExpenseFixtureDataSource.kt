package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class RecurringExpenseFixtureDataSource : RecurringExpenseDataSource {
    var rule =
        RecurringExpenseDomain(
            id = "rule",
            title = "Rent",
            amountMinor = 3000,
            paidBy = "you",
            category = "Other",
            nextOn = LocalDate.parse("2030-01-31"),
            anchorDay = 31,
            active = false,
            cancelled = false,
            createdBy = "you",
            version = 7,
            shares = mapOf("you" to 1500L, "alex" to 1500L),
            accountId = "home",
            splitMethod = ExpenseSplitMethodDomain.EQUAL,
            splitValues = emptyMap(),
        )
    val saves = mutableListOf<RecurringExpenseDomain>()
    var saveGate: CompletableDeferred<AppResult<RecurringExpenseDomain>>? = null
    var saveResult: AppResult<RecurringExpenseDomain>? = null

    override fun stream(accountId: String): Flow<AppResult<List<RecurringExpenseDomain>>> =
        error("Not used")

    override suspend fun getRule(id: String, accountId: String): AppResult<RecurringExpenseDomain> =
        AppResult.Success(rule)

    override suspend fun save(rule: RecurringExpenseDomain): AppResult<RecurringExpenseDomain> {
        saves += rule
        return saveGate?.await()
            ?: saveResult
            ?: AppResult.Success(rule.copy(version = rule.version + 1))
    }

    override suspend fun delete(rule: RecurringExpenseDomain): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun controlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain> = error("Not used")
}
