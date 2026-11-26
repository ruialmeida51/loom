package com.eyther.lumbridge.features.account.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.errors.AccountError
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain

class SavePaymentHandlesUseCase(private val paymentHandlesDataSource: PaymentHandlesDataSource) {
    suspend operator fun invoke(handles: PaymentHandlesDomain): AppResult<PaymentHandlesDomain> {
        val clean = handles.normalized()
        if (!clean.isValid()) return AppResult.Failure(AccountError.InvalidPaymentHandle())
        return paymentHandlesDataSource.save(handles = clean)
    }
}
