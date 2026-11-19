package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.MemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain

class DemoMemberAccountPositionDataSource(private val store: DemoHouseholdStore) :
    MemberAccountPositionDataSource {
    override suspend fun get(memberId: String): AppResult<List<MemberAccountPositionDomain>> =
        safeCall {
            store.expenseAccounts.value
                .filter { memberId in it.memberIds }
                .map { account ->
                    val balances =
                        HouseholdLedgerDomain.balances(
                            expenses = store.expenses.value.filter { it.accountId == account.id },
                            settlements =
                                store.settlements.value.filter { it.accountId == account.id },
                        )
                    MemberAccountPositionDomain(
                        accountId = account.id,
                        accountName = account.name,
                        isDefault = account.isDefault,
                        currency = account.currency,
                        balanceMinor = balances[memberId] ?: 0L,
                    )
                }
        }
}
