package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerRecordDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** An account's ledger held in memory, paged the way the server pages it. */
class LedgerFixtureDataSource : ExpenseDataSource {
    var expenses: List<ExpenseDomain> = emptyList()
    var settlements: List<SettlementDomain> = emptyList()

    /** What the server says, deliberately independent of the records it pages. */
    var balances: Map<String, Long> = emptyMap()
    var pageFailure: AppError? = null

    /** Every page read: the cursor it started after (null for the newest) and its limit. */
    val pageReads = mutableListOf<Pair<LedgerCursorDomain?, Int>>()
    private val writes = MutableStateFlow(0)

    /** Changes the ledger the way this app's own write would, reloading open streams. */
    fun write(change: LedgerFixtureDataSource.() -> Unit) {
        change()
        writes.update { oldVersion -> oldVersion + 1 }
    }

    override fun streamBalances(accountId: String): Flow<AppResult<Map<String, Long>>> =
        writes.map { AppResult.Success(balances) }

    override fun streamLedgerPage(
        accountId: String,
        limit: Int,
    ): Flow<AppResult<LedgerPageDomain>> =
        writes.map {
            pageReads += null to limit
            AppResult.Success(page(after = null, limit = limit))
        }

    override suspend fun getLedgerPage(
        accountId: String,
        after: LedgerCursorDomain,
        limit: Int,
    ): AppResult<LedgerPageDomain> {
        pageReads += after to limit
        pageFailure?.let {
            return AppResult.Failure(it)
        }
        return AppResult.Success(page(after = after, limit = limit))
    }

    private fun page(after: LedgerCursorDomain?, limit: Int): LedgerPageDomain =
        LedgerPageDomain.merge(
            expenses =
                expenses.filter {
                    after == null || LedgerRecordDomain.Purchase(expense = it).isAfter(after)
                },
            settlements =
                settlements.filter {
                    after == null || LedgerRecordDomain.Repayment(settlement = it).isAfter(after)
                },
            limit = limit,
        )

    override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
        error("Not used")

    override fun streamSettlements(accountId: String): Flow<AppResult<List<SettlementDomain>>> =
        error("Not used")

    override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain> =
        error("Not used")

    override suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain> =
        error("Not used")

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
        error("Not used")

    override suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain> =
        error("Not used")

    override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> =
        error("Not used")

    companion object {
        val account =
            ExpenseAccountDomain(
                id = "home",
                name = "Everyday",
                currency = "EUR",
                memberIds = setOf("you", "alex"),
                isDefault = true,
            )
        private val start = Instant.parse("2026-11-26T12:00:00Z")

        /** Entered [secondsAgo] before the fixture's clock; equal values share an instant. */
        fun expense(id: String, secondsAgo: Int, amountMinor: Long = 100): ExpenseDomain =
            ExpenseDomain(
                id = id,
                title = id,
                amountMinor = amountMinor,
                paidBy = "you",
                category = "Other",
                occurredOn = "2026-11-20",
                shares = mapOf("alex" to amountMinor),
                accountId = account.id,
                createdAt = (start - secondsAgo.seconds).toString(),
            )

        fun settlement(id: String, secondsAgo: Int): SettlementDomain =
            SettlementDomain(
                id = id,
                from = "alex",
                to = "you",
                amountMinor = 100,
                occurredOn = "2026-11-20",
                accountId = account.id,
                createdAt = (start - secondsAgo.seconds).toString(),
            )

        val accounts: ExpenseAccountDataSource =
            object : ExpenseAccountDataSource {
                override fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>> =
                    flowOf(AppResult.Success(listOf(account)))

                override fun streamSelection(): StateFlow<String> = MutableStateFlow(account.id)

                override suspend fun getAccount(id: String): AppResult<ExpenseAccountDomain> =
                    error("Not used")

                override suspend fun selectAccount(id: String): AppResult<Unit> = error("Not used")

                override suspend fun saveAccount(
                    account: ExpenseAccountDomain,
                ): AppResult<ExpenseAccountDomain> = error("Not used")

                override suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit> =
                    error("Not used")

                override suspend fun saveGuest(
                    accountId: String,
                    guest: ExpenseGuestDomain,
                ): AppResult<ExpenseAccountDomain> = error("Not used")

                override suspend fun setIcon(
                    accountId: String,
                    icon: ExpenseAccountIconDomain?,
                ): AppResult<ExpenseAccountDomain> = error("Not used")

                override suspend fun removeGuest(
                    accountId: String,
                    guest: ExpenseGuestDomain,
                ): AppResult<ExpenseAccountDomain> = error("Not used")
            }
    }
}
