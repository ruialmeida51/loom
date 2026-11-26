package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain

internal class PaymentHandlesFixtureDataSource(
    var handles: List<PaymentHandlesDomain> = emptyList(),
) : PaymentHandlesDataSource {
    override suspend fun get(userIds: Collection<String>): AppResult<List<PaymentHandlesDomain>> =
        AppResult.Success(handles.filter { it.userId in userIds })

    override suspend fun save(handles: PaymentHandlesDomain): AppResult<PaymentHandlesDomain> =
        AppResult.Success(handles)
}
