package com.eyther.lumbridge.features.account.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain

class GetPaymentHandlesUseCase(private val paymentHandlesDataSource: PaymentHandlesDataSource) {
    suspend operator fun invoke(userIds: Collection<String>): AppResult<List<PaymentHandlesDomain>> =
        if (userIds.isEmpty()) AppResult.Success(emptyList())
        else paymentHandlesDataSource.get(userIds = userIds)
}
