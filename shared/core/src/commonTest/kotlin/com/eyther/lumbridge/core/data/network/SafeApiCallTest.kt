package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class SafeApiCallTest {
    @Test
    fun readStreamsRecoverAfterTransientFailuresWithoutLosingTheFailureEvent() = runTest {
        var attempts = 0
        val results = flow {
            attempts++
            if (attempts == 1) throw IOException("temporarily offline")
            emit(42)
        }
            .asApiResult(retryTransientFailures = true)
            .toList()
        assertIs<CommonError.NoConnection>(assertIs<AppResult.Failure>(results.first()).error)
        assertEquals(42, assertIs<AppResult.Success<Int>>(results.last()).value)
        assertEquals(2, attempts)
    }

    @Test
    fun authorizationFailuresNeverEnterTheTransportRetryLoop() = runTest {
        var attempts = 0
        val results =
            flow<Int> {
                    attempts++
                    throw CommonError.AccessDenied()
                }
                .asApiResult(retryTransientFailures = true)
                .toList()
        assertIs<CommonError.AccessDenied>(assertIs<AppResult.Failure>(results.single()).error)
        assertEquals(1, attempts)
    }

    @Test
    fun domainFailuresKeepTheirTypeAndCauseAcrossUseCases() = runTest {
        val cause = IOException("diagnostic transport detail")
        val error = CommonError.NoConnection(cause = cause)
        val dataResult = safeApiCall<Unit> { throw error }
        val domainResult = safeCall { dataResult.getOrThrow() }

        assertSame(error, assertIs<AppResult.Failure>(domainResult).error)
        assertSame(cause, error.cause)
        assertSame(error, assertFailsWith<CommonError.NoConnection> { domainResult.getOrThrow() })
    }

    @Test
    fun arbitraryExceptionTextCannotBecomeARecognizedDomainFailure() = runTest {
        val cause =
            IllegalStateException(
                "shopping.message_this_item_changed_refresh_before_undoing_or_editing_it",
            )
        val result = safeApiCall<Unit> { throw cause }
        val error = assertIs<CommonError.Unknown>(assertIs<AppResult.Failure>(result).error)

        assertSame(cause, error.cause)
    }

    @Test
    fun cancellationEscapesDataAndDomainBoundaries() = runTest {
        val cancellation = CancellationException("caller left the screen")

        assertFailsWith<CancellationException> {
            safeApiCall<Unit> { throw cancellation }
        }
        assertFailsWith<CancellationException> {
            safeCall<Unit> { throw cancellation }
        }
    }

    @Test
    fun streamsEmitTypedFailuresAfterSuccessfulValues() = runTest {
        val cause = IOException("connection lost")
        val results = flow {
            emit(7)
            throw cause
        }
            .asApiResult()
            .toList()

        assertEquals(7, assertIs<AppResult.Success<Int>>(results.first()).value)
        val error =
            assertIs<CommonError.NoConnection>(assertIs<AppResult.Failure>(results.last()).error)
        assertSame(cause, error.cause)
    }

    @Test
    fun streamsNeverConvertCancellationIntoFailureValues() = runTest {
        val cancellation = CancellationException("household changed")
        val values = mutableListOf<AppResult<Int>>()

        assertFailsWith<CancellationException> {
            flow<Int> { throw cancellation }.asApiResult().toList(values)
        }
        assertEquals(emptyList(), values)
    }
}
