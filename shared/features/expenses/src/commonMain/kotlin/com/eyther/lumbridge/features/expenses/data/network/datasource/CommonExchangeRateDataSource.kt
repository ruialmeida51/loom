package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExchangeRateResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExchangeRateDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Daily reference rates from Frankfurter, which republishes the European Central Bank's rates
 * with no key. Only the two currency codes leave the phone. A rate is kept for the session, since
 * the bank publishes once a day.
 */
class CommonExchangeRateDataSource(private val client: HttpClient) : ExchangeRateDataSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val lock = Mutex()
    private val cache = mutableMapOf<Pair<String, String>, String>()

    override suspend fun latestRate(from: String, to: String): AppResult<String> {
        lock.withLock { cache[from to to] }?.let { return AppResult.Success(it) }
        return try {
            val response =
                client.get(RATES_URL) {
                    parameter("base", from)
                    parameter("symbols", to)
                }
            if (!response.status.isSuccess()) return AppResult.Failure(ExpenseError.RateUnavailable())
            val rate =
                json
                    .decodeFromString<ExchangeRateResponse>(response.bodyAsText())
                    .rates[to]
                    ?.content
                    ?.let { OriginalAmountDomain.normalizeRate(it) }
                    ?: return AppResult.Failure(ExpenseError.RateUnavailable())
            lock.withLock { cache[from to to] = rate }
            AppResult.Success(rate)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            AppResult.Failure(ExpenseError.RateUnavailable(cause = error))
        }
    }

    private companion object {
        const val RATES_URL = "https://api.frankfurter.dev/v1/latest"
    }
}
