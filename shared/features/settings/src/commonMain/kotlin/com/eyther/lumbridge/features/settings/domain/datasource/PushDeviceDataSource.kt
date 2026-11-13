package com.eyther.lumbridge.features.settings.domain.datasource

/** Notification preferences own registration; authentication coordinates session changes. */
interface PushDeviceDataSource {
    suspend fun syncRegistration()

    suspend fun unregisterCurrentDevice()
}
