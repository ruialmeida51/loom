package com.eyther.lumbridge.features.account.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain

interface PaymentHandlesDataSource {
    /** The payment details these people share with the signed-in person; missing ones are absent. */
    suspend fun get(userIds: Collection<String>): AppResult<List<PaymentHandlesDomain>>

    /** Saves the signed-in person's details, or removes them when [handles] is empty. */
    suspend fun save(handles: PaymentHandlesDomain): AppResult<PaymentHandlesDomain>
}
