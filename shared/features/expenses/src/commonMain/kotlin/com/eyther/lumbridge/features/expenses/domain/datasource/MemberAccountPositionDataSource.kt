package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain

interface MemberAccountPositionDataSource {
    suspend fun get(memberId: String): AppResult<List<MemberAccountPositionDomain>>
}
