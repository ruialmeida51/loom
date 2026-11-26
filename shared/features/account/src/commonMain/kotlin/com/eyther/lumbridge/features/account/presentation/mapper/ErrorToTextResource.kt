package com.eyther.lumbridge.features.account.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.domain.errors.AccountError
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.invalid_nickname
import com.eyther.lumbridge.features.account.generated.resources.invalid_payment_handle

internal fun AppError.toAccountTextResource(): TextResource =
    when (this) {
        is AccountError.InvalidNickname -> TextResource.Resource(Res.string.invalid_nickname)
        is AccountError.InvalidPaymentHandle ->
            TextResource.Resource(Res.string.invalid_payment_handle)
        else -> toTextResource()
    }
