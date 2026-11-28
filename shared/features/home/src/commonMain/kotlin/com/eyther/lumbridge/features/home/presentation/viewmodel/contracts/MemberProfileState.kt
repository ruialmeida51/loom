package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.presentation.model.PayOptionsUi
import com.eyther.lumbridge.features.home.domain.model.MemberProfileDomain
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.home.presentation.model.MemberSettlementUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** [profile] is null while loading and once the member has left ([missing]). */
data class MemberProfileState(
    val userId: String,
    val currency: String,
    val today: String,
    val timeZone: String,
    val profile: MemberProfileDomain? = null,
    val missing: Boolean = false,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val loadError: TextResource? = null,
) {
    val isCurrentUser: Boolean = profile?.member?.id == userId

    val settlements: ImmutableList<MemberSettlementUi> =
        profile?.let { profile ->
                val names = profile.members.associate { it.id to it.name.ifBlank { null } }
                val pictures = profile.members.associate { it.id to it.avatarPath }
                profile.settlements.map { settlement ->
                    val other =
                        if (settlement.from == profile.member.id) settlement.to else settlement.from
                    MemberSettlementUi(
                        fromName = names[settlement.from],
                        toName = names[settlement.to],
                        fromIsYou = settlement.from == userId,
                        toIsYou = settlement.to == userId,
                        amount = MoneyDomain(settlement.amountMinor, currency).formattedMagnitude(),
                        otherId = other,
                        otherName = names[other],
                        otherAvatarPath = pictures[other],
                    )
                }
            }
            .orEmpty()
            .toImmutableList()

    /**
     * Ways to pay this member, offered to everyone else. When you owe them on the everyday account
     * PayPal opens with that amount filled in; otherwise it opens their page.
     */
    val payOptions: PayOptionsUi? =
        profile
            ?.takeUnless { isCurrentUser }
            ?.let { profile ->
                profile.handles?.let { handles ->
                    PayOptionsUi(
                        recipientName = profile.member.name,
                        handles = handles,
                        amount =
                            profile.settlements
                                .firstOrNull { it.from == userId && it.to == profile.member.id }
                                ?.let { MoneyDomain(it.amountMinor, currency).inputText() },
                        currency = currency,
                        linkWithoutAmount = true,
                    )
                }
            }

    val tasks: ImmutableList<HomeTaskUi> =
        profile
            ?.openTasks
            ?.map { it.toHomeUi(today = today, timeZone = timeZone) }
            ?.toImmutableList() ?: persistentListOf()
}
