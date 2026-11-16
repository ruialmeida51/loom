package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class JoinHouseholdResponse(@SerialName("status") val status: String)
