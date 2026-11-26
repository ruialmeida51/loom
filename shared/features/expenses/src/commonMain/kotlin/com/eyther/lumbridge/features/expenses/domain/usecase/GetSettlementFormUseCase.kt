package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.SettlementFormDomain
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Resolves a household-scoped payment and the account's participant references together. */
class GetSettlementFormUseCase(
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val getSettlementUseCase: GetSettlementUseCase,
    private val householdDataSource: HouseholdDataSource,
    private val context: HouseholdContextDomain,
    private val getPaymentHandlesUseCase: GetPaymentHandlesUseCase,
) {
    suspend operator fun invoke(
        accountId: String,
        settlementId: String?,
        refreshReferences: Boolean = false,
    ): AppResult<SettlementFormDomain> = safeCall {
        coroutineScope {
            val accountRequest = async {
                if (refreshReferences)
                    expenseAccountDataSource.getAccount(id = accountId).getOrThrow()
                else expenseAccountDataSource.getReferenceAccount(id = accountId).getOrThrow()
            }
            val membersRequest = async {
                householdDataSource.getMemberHistory(householdId = context.id).getOrThrow()
            }
            val settlementRequest = async {
                settlementId?.let {
                    getSettlementUseCase(id = it, accountId = accountId).getOrThrow()
                }
            }
            val account = accountRequest.await()
            val members =
                account.withGuests(membersRequest.await()).filter {
                    it.id in account.participantIds
                }
            // Payment details only make paying easier; the form works without them.
            val handles =
                (getPaymentHandlesUseCase(userIds = account.memberIds) as? AppResult.Success)
                    ?.value
                    .orEmpty()
            SettlementFormDomain(
                account = account,
                members = members,
                settlement = settlementRequest.await(),
                paymentHandles = handles.associateBy { it.userId },
            )
        }
    }
}
