package com.eyther.lumbridge.features.account.domain.model

/** [avatarPath] is the stored profile picture, or null for initials. */
data class ProfileDomain(val id: String, val nickname: String, val avatarPath: String? = null)
