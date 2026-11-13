package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.*
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoRecurringExpenseDataSource(private val store: DemoHouseholdStore) :
    RecurringExpenseDataSource {
    override fun observe() = store.recurring

    override suspend fun control(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ) = safeCall {
        val current = store.recurring.value.first { it.id == rule.id }
        require(current.version == rule.version && !current.cancelled)
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
