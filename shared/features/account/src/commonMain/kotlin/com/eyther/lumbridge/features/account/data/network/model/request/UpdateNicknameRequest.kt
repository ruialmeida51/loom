package com.eyther.lumbridge.features.account.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateNicknameRequest(@SerialName("display_name") val displayName: String)
