package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoRecurringExpenseDataSource(private val store: DemoHouseholdStore) :
    RecurringExpenseDataSource {
    override fun stream(accountId: String) =
        store.recurring.map { rules ->
            AppResult.Success(
                rules
                    .filter { it.accountId.ifBlank { store.context.id } == accountId }
                    .map { it.copy(accountId = accountId) },
            )
        }

    override suspend fun getRule(id: String, accountId: String): AppResult<RecurringExpenseDomain> =
        safeCall {
            store.recurring.value.firstOrNull { it.id == id && it.accountId == accountId }
                ?: throw RecurringExpenseError.NotFound()
        }

    override suspend fun save(rule: RecurringExpenseDomain): AppResult<RecurringExpenseDomain> =
        safeCall {
            val current =
                store.recurring.value.firstOrNull { it.id == rule.id }
                    ?: throw RecurringExpenseError.NotFound()
            if (current.version != rule.version) throw RecurringExpenseError.Changed()
            if (current.cancelled) throw RecurringExpenseError.Cancelled()
            val saved = rule.copy(active = current.active, version = current.version + 1)
            store.recurring.update { rules -> rules.map { if (it.id == saved.id) saved else it } }
            saved
        }

    override suspend fun delete(rule: RecurringExpenseDomain): AppResult<Unit> = safeCall {
        val current = store.recurring.value.firstOrNull { it.id == rule.id } ?: return@safeCall Unit
        if (current.version != rule.version) throw RecurringExpenseError.Changed()
        store.recurring.update { rules -> rules.filterNot { it.id == rule.id } }
    }

    override suspend fun controlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ) = safeCall {
        val current =
            store.recurring.value.firstOrNull { it.id == rule.id }
                ?: throw RecurringExpenseError.NotFound()
        if (current.cancelled) throw RecurringExpenseError.Cancelled()
        if (current.version != rule.version) throw RecurringExpenseError.Changed()
        val saved =
            current.copy(
                active = action == RecurringExpenseActionDomain.RESUME,
                cancelled = action == RecurringExpenseActionDomain.CANCEL,
                version = current.version + 1,
                nextOn =
                    if (action == RecurringExpenseActionDomain.RESUME)
                        MonthlyRecurrenceDomain.resumeOn(
                                LocalDate.parse(current.nextOn),
                                LocalDate.parse(todayIso(store.context.timeZone)),
                                current.anchorDay,
                            )
                            .toString()
                    else current.nextOn,
            )
        store.recurring.update { it.map { old -> if (old.id == saved.id) saved else old } }
        saved
    }
}
