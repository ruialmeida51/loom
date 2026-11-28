package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

/**
 * One housemate as the household sees them: who they are, how they like to be paid, where they
 * stand on the everyday account (the one Home shows) and the tasks waiting on them.
 * [handles] is null when they share no payment details.
 */
data class MemberProfileDomain(
    val member: MemberDomain,
    /** Everyone in the household, to name the other side of each repayment. */
    val members: List<MemberDomain>,
    val handles: PaymentHandlesDomain?,
    val balances: Map<String, Long>,
    val tasks: List<TaskDomain>,
) {
    /** The repayments that would settle the everyday account and involve this member. */
    val settlements: List<SuggestedSettlementDomain> =
        (safeCall { HouseholdLedgerDomain.suggestions(balances) } as? AppResult.Success)
            ?.value
            .orEmpty()
            .filter { it.from == member.id || it.to == member.id }

    /** Their open tasks, soonest first and undated ones last. */
    val openTasks: List<TaskDomain> =
        tasks
            .filter { !it.completed && it.assigneeId == member.id }
            .sortedWith(
                compareBy<TaskDomain> { it.dueAt == null }.thenBy { it.dueAt }.thenBy { it.id },
            )
}
