package com.eyther.lumbridge.core.data

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The data boundary owns I/O dispatch and maps exceptions to the domain result. */
suspend fun <T> safeApiCall(action: suspend () -> T): AppResult<T> =
    withContext(Dispatchers.IO) { safeCall(action) }
