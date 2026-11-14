package com.eyther.lumbridge.features.settings.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult

interface PushDeviceDataSource {

    suspend fun syncRegistration(): AppResult<Unit>

    suspend fun unregisterCurrentDevice(): AppResult<Unit>
}
