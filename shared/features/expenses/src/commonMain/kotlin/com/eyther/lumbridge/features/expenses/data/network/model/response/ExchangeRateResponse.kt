package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal data class ExchangeRateResponse(
    @SerialName("base") val base: String,
    @SerialName("date") val date: String,
    @SerialName("rates") val rates: Map<String, JsonPrimitive>,
)
