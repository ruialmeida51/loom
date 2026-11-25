package com.eyther.lumbridge.features.account.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A null [path] clears the picture. */
@Serializable
internal data class SetProfilePictureRequest(@SerialName("p_path") val path: String?)
