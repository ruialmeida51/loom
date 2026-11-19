package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.MemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain

class GetMemberAccountPositionsUseCase(
    private val memberAccountPositionDataSource: MemberAccountPositionDataSource,
) {
    suspend operator fun invoke(memberId: String): AppResult<List<MemberAccountPositionDomain>> =
        memberAccountPositionDataSource.get(memberId = memberId)
}
