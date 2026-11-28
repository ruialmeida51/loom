package com.eyther.lumbridge.features.home.presentation.model

import androidx.compose.runtime.Immutable

/**
 * One repayment involving the profile's member, as "[fromName] owes [toName] [amount]". The
 * viewer is "you" on either side ([fromIsYou], [toIsYou]); the avatar is the other person, the
 * one who isn't the profile's member.
 */
@Immutable
data class MemberSettlementUi(
    val fromName: String?,
    val toName: String?,
    val fromIsYou: Boolean,
    val toIsYou: Boolean,
    val amount: String,
    val otherId: String,
    val otherName: String?,
    val otherAvatarPath: String?,
)
