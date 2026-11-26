package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource
import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource.Companion.expense
import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource.Companion.settlement
import com.eyther.lumbridge.features.expenses.domain.model.AccountLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerRecordDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetAccountBalancesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetAccountLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSelectedExpenseAccountStreamUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AccountLedgerStreamTest {
    private val source =
        LedgerFixtureDataSource().apply {
            // 130 expenses and 30 repayments; pairs of records share their entry instant.
            expenses = (1..130).map { expense(id = "e${padded(it)}", secondsAgo = it / 2) }
            settlements = (1..30).map { settlement(id = "s${padded(it)}", secondsAgo = it * 3) }
            balances = mapOf("alex" to -98_765L, "you" to 98_765L)
        }
    private val loadMore = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val results = mutableListOf<AppResult<AccountLedgerDomain>>()
    private val useCase =
        GetAccountLedgerStreamUseCase(
            expenseDataSource = source,
            getAccountBalancesStreamUseCase =
                GetAccountBalancesStreamUseCase(expenseDataSource = source),
            getSelectedExpenseAccountStreamUseCase =
                GetSelectedExpenseAccountStreamUseCase(
                    expenseAccountDataSource = LedgerFixtureDataSource.accounts,
                ),
        )
    private val latest: AccountLedgerDomain
        get() = results.last().getOrThrow()

    private fun padded(number: Int): String = number.toString().padStart(3, '0')

    private fun allKeys(): List<String> =
        (source.expenses.map { LedgerRecordDomain.Purchase(expense = it) } +
                source.settlements.map { LedgerRecordDomain.Repayment(settlement = it) })
            .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
            .map { it.key }

    private fun TestScope.collect(initialSize: Int = LedgerPageDomain.PAGE_SIZE) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(loadMoreRequests = loadMore, initialSize = initialSize).collect { results += it }
        }
        runCurrent()
    }

    @Test
    fun theFirstPageIsTheNewestFiftyAndBalancesComeFromTheServer() = runTest {
        collect()
        assertEquals(allKeys().take(50), latest.history.records.map { it.key })
        assertNotNull(latest.history.nextCursor)
        assertEquals(
            mapOf("alex" to -98_765L, "you" to 98_765L),
            latest.balances,
            "Balances cover the whole history, not the loaded page",
        )
        assertEquals(listOf<Pair<LedgerCursorDomain?, Int>>(null to 50), source.pageReads)
    }

    @Test
    fun scrollingLoadsOlderPagesUntilTheWholeHistoryIsShownOnce() = runTest {
        collect()
        loadMore.tryEmit(Unit)
        runCurrent()
        assertTrue(results.any { (it as? AppResult.Success)?.value?.loadingMore == true })
        assertEquals(allKeys().take(100), latest.history.records.map { it.key })
        assertEquals(false, latest.loadingMore)
        repeat(3) {
            loadMore.tryEmit(Unit)
            runCurrent()
        }
        assertEquals(allKeys(), latest.history.records.map { it.key })
        assertNull(latest.history.nextCursor)
        val reads = source.pageReads.size
        loadMore.tryEmit(Unit)
        runCurrent()
        assertEquals(reads, source.pageReads.size, "Nothing is left to load")
    }

    @Test
    fun thisAppsWritesReloadTheNewestPageAndKeepTheOlderPagesShown() = runTest {
        collect()
        loadMore.tryEmit(Unit)
        runCurrent()
        val edited = source.expenses[80]
        val deleted = source.expenses[10]
        source.pageReads.clear()
        source.write {
            expenses =
                listOf(expense("new", secondsAgo = -60)) +
                    expenses.filterNot { it.id == deleted.id }.map {
                        if (it.id == edited.id) it.copy(amountMinor = 4_200) else it
                    }
            balances = mapOf("alex" to -1L, "you" to 1L)
        }
        runCurrent()
        val records = latest.history.records
        assertEquals(allKeys().take(100), records.map { it.key })
        assertEquals("expense-new", records.first().key)
        assertTrue(records.none { it.id == deleted.id })
        val shown = records.single { it.id == edited.id }
        assertEquals(4_200, assertIs<LedgerRecordDomain.Purchase>(shown).expense.amountMinor)
        assertEquals(mapOf("alex" to -1L, "you" to 1L), latest.balances)
        assertEquals(listOf(null, records[49].cursor), source.pageReads.map { it.first })
    }

    @Test
    fun restartingRestoresAsManyRecordsAsWereShownInLargeChunks() = runTest {
        collect(initialSize = 140)
        assertEquals(allKeys().take(140), latest.history.records.map { it.key })
        assertEquals(listOf(50, 90), source.pageReads.map { it.second })
    }

    @Test
    fun aFailedPageKeepsTheLoadedRecordsAndCanBeRetried() = runTest {
        collect()
        source.pageFailure = CommonError.NoConnection()
        loadMore.tryEmit(Unit)
        runCurrent()
        // Only the older page failed: the loaded records stay and the error rides along.
        assertIs<CommonError.NoConnection>(latest.loadMoreError)
        assertEquals(allKeys().take(50), latest.history.records.map { it.key })
        source.pageFailure = null
        loadMore.tryEmit(Unit)
        runCurrent()
        assertEquals(allKeys().take(100), latest.history.records.map { it.key })
        assertNull(latest.loadMoreError)
    }
}
