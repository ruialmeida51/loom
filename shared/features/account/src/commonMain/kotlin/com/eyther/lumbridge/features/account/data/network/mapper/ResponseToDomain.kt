package com.eyther.lumbridge.features.account.data.network.mapper

import com.eyther.lumbridge.features.account.data.network.model.response.PaymentHandlesResponse
import com.eyther.lumbridge.features.account.data.network.model.response.ProfileResponse
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain

internal fun ProfileResponse.toDomain(): ProfileDomain =
    ProfileDomain(id = id, nickname = displayName, avatarPath = avatarPath)

internal fun PaymentHandlesResponse.toDomain(): PaymentHandlesDomain =
    PaymentHandlesDomain(
        userId = userId,
        mbwayPhone = mbwayPhone,
        revolutUsername = revolutUsername,
        paypalUsername = paypalUsername,
    )
