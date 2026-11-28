package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toBalances
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRemoveRequest
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRequest
import com.eyther.lumbridge.features.expenses.data.network.mapper.toSaveRequest
import com.eyther.lumbridge.features.expenses.data.network.mapper.toSpending
import com.eyther.lumbridge.features.expenses.data.network.model.request.DeleteExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.AccountBalanceResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.AccountSpendingResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.SettlementResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class CommonExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseDataSource {
    override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
        access
            .watch {
                selectAll(id = { it.id }) { afterId ->
                    access
                        .requireClient()
                        .from("account_expenses_with_shares")
                        .select {
                            filter {
                                eq("household_id", context.id)
                                eq("account_id", accountId.ifBlank { context.id })
                                afterId?.let { gt("id", it) }
                            }
                            order(column = "id", order = Order.ASCENDING)
                            limit(count = FULL_HISTORY_CHUNK.toLong())
                        }
                        .decodeList<ExpenseResponse>()
                        .map { it.toDomain() }
                }
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override fun streamSettlements(accountId: String): Flow<AppResult<List<SettlementDomain>>> =
        access
            .watch {
                selectAll(id = { it.id }) { afterId ->
                    access
                        .requireClient()
                        .from("settlements")
                        .select {
                            filter {
                                eq("household_id", context.id)
                                eq("account_id", accountId.ifBlank { context.id })
                                afterId?.let { gt("id", it) }
                            }
                            order(column = "id", order = Order.ASCENDING)
                            limit(count = FULL_HISTORY_CHUNK.toLong())
                        }
                        .decodeList<SettlementResponse>()
                        .map { it.toDomain() }
                }
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override fun streamBalances(accountId: String): Flow<AppResult<Map<String, Long>>> =
        access
            .watch {
                access
                    .requireClient()
                    .from("account_balances")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId.ifBlank { context.id })
                        }
                    }
                    .decodeList<AccountBalanceResponse>()
                    .toBalances()
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override suspend fun getSpending(accountId: String): AppResult<AccountSpendingDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.read {
                access
                    .requireClient()
                    .from("account_spending")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId.ifBlank { context.id })
                        }
                    }
                    .decodeList<AccountSpendingResponse>()
                    .toSpending()
            }
        }

    override fun streamLedgerPage(
        accountId: String,
        limit: Int,
    ): Flow<AppResult<LedgerPageDomain>> =
        access
            .watch { selectLedgerPage(accountId = accountId, after = null, limit = limit) }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override suspend fun getLedgerPage(
        accountId: String,
        after: LedgerCursorDomain,
        limit: Int,
    ): AppResult<LedgerPageDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.read { selectLedgerPage(accountId = accountId, after = after, limit = limit) }
        }

    /**
     * Keyset pagination on (created_at desc, id) rather than offsets: records added or deleted
     * while someone scrolls do not shift later pages, so nothing is skipped or shown twice, and
     * each page reads from the newest-first index instead of counting past every earlier row.
     * Both tables take the same cursor, and one extra candidate from each says whether more exist.
     */
    private suspend fun selectLedgerPage(
        accountId: String,
        after: LedgerCursorDomain?,
        limit: Int,
    ): LedgerPageDomain = coroutineScope {
        val client = access.requireClient()
        val scope = accountId.ifBlank { context.id }
        val expenses = async {
            client
                .from("account_expenses_with_shares")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", scope)
                        if (after != null) enteredBefore(cursor = after)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    order(column = "id", order = Order.ASCENDING)
                    limit(count = limit + 1L)
                }
                .decodeList<ExpenseResponse>()
                .map { it.toDomain() }
        }
        val settlements = async {
            client
                .from("settlements")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", scope)
                        if (after != null) enteredBefore(cursor = after)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    order(column = "id", order = Order.ASCENDING)
                    limit(count = limit + 1L)
                }
                .decodeList<SettlementResponse>()
                .map { it.toDomain() }
        }
        LedgerPageDomain.merge(
            expenses = expenses.await(),
            settlements = settlements.await(),
            limit = limit,
        )
    }

    /** Rows after [cursor] in (created_at desc, id) order. */
    private fun PostgrestFilterBuilder.enteredBefore(cursor: LedgerCursorDomain) {
        or {
            lt("created_at", cursor.createdAt.toString())
            and {
                eq("created_at", cursor.createdAt.toString())
                gt("id", cursor.id)
            }
        }
    }

    /**
     * Reads every row in id order, a chunk at a time: PostgREST caps each response (1000 rows by
     * default), so one request would silently drop records of a long history. Like household's
     * `selectHouseholdRows`, each chunk starts after the last id, so concurrent writes never shift
     * a later chunk.
     */
    private suspend fun <T> selectAll(
        id: (T) -> String,
        chunk: suspend (afterId: String?) -> List<T>,
    ): List<T> {
        val rows = mutableListOf<T>()
        do {
            val next = chunk(rows.lastOrNull()?.let(id))
            rows += next
        } while (next.size == FULL_HISTORY_CHUNK)
        return rows
    }

    override suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .from("account_expenses_with_shares")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", accountId)
                        eq("id", id)
                    }
                }
                .decodeSingle<ExpenseResponse>()
                .toDomain()
        }

    override suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .from("settlements")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", accountId)
                        eq("id", id)
                    }
                }
                .decodeSingle<SettlementResponse>()
                .toDomain()
        }

    override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc("save_account_expense", expense.toRequest(householdId = context.id))
            }
            Unit
        }

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            val request =
                DeleteExpenseRequest(
                    householdId = context.id,
                    accountId = expense.accountId,
                    id = expense.id,
                    version = expense.version,
                )
            access.write { client ->
                client.postgrest.rpc("delete_account_expense", request)
            }
            Unit
        }

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc("record_account_settlement", settlement.toRequest(householdId = context.id))
            }
            Unit
        }

    override suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "save_account_settlement",
                        settlement.toSaveRequest(householdId = context.id),
                    ) {
                        single()
                    }
                    .decodeAs<SettlementResponse>()
                    .toDomain()
            }
        }

    override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "delete_account_settlement",
                        settlement.toRemoveRequest(householdId = context.id),
                    )
            }
            Unit
        }

    private companion object {
        /** Below PostgREST's default response cap, so a full chunk always means more may follow. */
        const val FULL_HISTORY_CHUNK = 500
    }
}
