package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Payment details for the preview: Alex can be paid with MB Way, Revolut and PayPal. */
class DemoPaymentHandlesDataSource : PaymentHandlesDataSource {
    private val handles =
        MutableStateFlow(
            mapOf(
                DemoHouseholdStore.ALEX to
                    PaymentHandlesDomain(
                        userId = DemoHouseholdStore.ALEX,
                        mbwayPhone = "+351912345678",
                        revolutUsername = "alexsample",
                        paypalUsername = "AlexSample",
                    ),
            ),
        )

    override suspend fun get(userIds: Collection<String>): AppResult<List<PaymentHandlesDomain>> =
        safeCall { userIds.mapNotNull { handles.value[it] } }

    override suspend fun save(handles: PaymentHandlesDomain) = safeCall {
        val saved = handles.copy(userId = DemoHouseholdStore.USER)
        this.handles.update { current ->
            if (saved.isEmpty) current - saved.userId else current + (saved.userId to saved)
        }
        saved
    }
}
