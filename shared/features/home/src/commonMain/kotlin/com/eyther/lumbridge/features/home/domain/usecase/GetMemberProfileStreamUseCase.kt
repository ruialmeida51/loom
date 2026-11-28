package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.home.domain.model.MemberProfileDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * A member's profile, or null once they have left the household. Payment details are optional
 * decoration: failing to load them shows the rest without them.
 */
class GetMemberProfileStreamUseCase(
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val getHomeBalanceStreamUseCase: GetHomeBalanceStreamUseCase,
    private val getTasksStreamUseCase: GetTasksStreamUseCase,
    private val getPaymentHandlesUseCase: GetPaymentHandlesUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(memberId: String): Flow<AppResult<MemberProfileDomain?>> =
        combine(
            getMembersStreamUseCase(householdId = context.id),
            getHomeBalanceStreamUseCase(),
            getTasksStreamUseCase(),
            flow { emit(loadHandles(memberId = memberId)) },
        ) { members, balances, tasks, handles ->
            safeCall {
                val everyone = members.getOrThrow()
                everyone.firstOrNull { it.id == memberId }?.let { member ->
                    MemberProfileDomain(
                        member = member,
                        members = everyone,
                        handles = handles,
                        balances = balances.getOrThrow(),
                        tasks = tasks.getOrThrow(),
                    )
                }
            }
        }

    private suspend fun loadHandles(memberId: String): PaymentHandlesDomain? =
        (getPaymentHandlesUseCase(userIds = listOf(memberId)) as? AppResult.Success)
            ?.value
            ?.firstOrNull()
            ?.takeUnless { it.isEmpty }
}
