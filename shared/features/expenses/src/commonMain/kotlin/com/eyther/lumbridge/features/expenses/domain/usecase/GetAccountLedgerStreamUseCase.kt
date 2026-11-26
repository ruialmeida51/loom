package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.AccountLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The selected account's Money overview: its newest records, one page at a time, beside the
 * balances the server adds up over the whole history.
 *
 * When this app writes, the newest page reloads and the older pages already shown are fetched
 * again, so edits deep in the list appear and the list does not shrink under the reader.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetAccountLedgerStreamUseCase(
    private val expenseDataSource: ExpenseDataSource,
    private val getAccountBalancesStreamUseCase: GetAccountBalancesStreamUseCase,
    private val getSelectedExpenseAccountStreamUseCase: GetSelectedExpenseAccountStreamUseCase,
) {
    private data class LoadedHistory(
        val page: LedgerPageDomain,
        val loadingMore: Boolean = false,
        val loadMoreError: AppError? = null,
    )

    /**
     * @param loadMoreRequests each emission loads the next older page, when there is one.
     * @param initialSize how many records the first load restores, so restarting the stream
     *   keeps a list the reader already scrolled through.
     */
    operator fun invoke(
        loadMoreRequests: Flow<Unit>,
        initialSize: Int = LedgerPageDomain.PAGE_SIZE,
    ): Flow<AppResult<AccountLedgerDomain>> =
        getSelectedExpenseAccountStreamUseCase().flatMapLatest { result ->
            when (result) {
                is AppResult.Failure -> flowOf(result)
                is AppResult.Success ->
                    combine(
                        getAccountBalancesStreamUseCase(accountId = result.value.id),
                        streamHistory(
                            accountId = result.value.id,
                            loadMoreRequests = loadMoreRequests,
                            initialSize = initialSize,
                        ),
                    ) { balances, history ->
                        safeCall {
                            val loaded = history.getOrThrow()
                            AccountLedgerDomain(
                                account = result.value,
                                history = loaded.page,
                                balances = balances.getOrThrow(),
                                loadingMore = loaded.loadingMore,
                                loadMoreError = loaded.loadMoreError,
                            )
                        }
                    }
            }
        }

    private fun streamHistory(
        accountId: String,
        loadMoreRequests: Flow<Unit>,
        initialSize: Int,
    ): Flow<AppResult<LoadedHistory>> = channelFlow {
        // One page operation at a time: a reload and a load-more never interleave their results.
        val paging = Mutex()
        var loaded: LedgerPageDomain? = null
        launch {
            loadMoreRequests.collect {
                paging.withLock {
                    val current = loaded ?: return@withLock
                    val cursor = current.nextCursor ?: return@withLock
                    send(AppResult.Success(LoadedHistory(page = current, loadingMore = true)))
                    val older =
                        expenseDataSource.getLedgerPage(
                            accountId = accountId,
                            after = cursor,
                            limit = LedgerPageDomain.PAGE_SIZE,
                        )
                    when (older) {
                        is AppResult.Success -> {
                            val next = current.append(older = older.value)
                            loaded = next
                            send(AppResult.Success(LoadedHistory(page = next)))
                        }
                        // The loaded pages stay; only the older page failed, and it is retried
                        // by the next load-more request.
                        is AppResult.Failure ->
                            send(
                                AppResult.Success(
                                    LoadedHistory(page = current, loadMoreError = older.error),
                                )
                            )
                    }
                }
            }
        }
        expenseDataSource
            .streamLedgerPage(accountId = accountId, limit = LedgerPageDomain.PAGE_SIZE)
            .collect { newest ->
                paging.withLock {
                    when (newest) {
                        is AppResult.Failure -> send(newest)
                        is AppResult.Success -> {
                            val restored =
                                restore(
                                    accountId = accountId,
                                    newest = newest.value,
                                    size = loaded?.records?.size ?: initialSize,
                                )
                            if (restored is AppResult.Success) loaded = restored.value
                            send(restored.mapToResult { LoadedHistory(page = it) })
                        }
                    }
                }
            }
    }

    /** Extends the newest page with older ones until it holds [size] records or none are left. */
    private suspend fun restore(
        accountId: String,
        newest: LedgerPageDomain,
        size: Int,
    ): AppResult<LedgerPageDomain> {
        var history = newest
        while (history.records.size < size) {
            val cursor = history.nextCursor ?: break
            val older =
                expenseDataSource.getLedgerPage(
                    accountId = accountId,
                    after = cursor,
                    limit = minOf(LedgerPageDomain.RELOAD_CHUNK, size - history.records.size),
                )
            history =
                when (older) {
                    is AppResult.Success -> history.append(older = older.value)
                    is AppResult.Failure -> return older
                }
        }
        return AppResult.Success(history)
    }
}
