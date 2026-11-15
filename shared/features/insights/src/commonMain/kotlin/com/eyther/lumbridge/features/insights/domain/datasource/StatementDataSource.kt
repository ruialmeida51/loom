package com.eyther.lumbridge.features.insights.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult

interface StatementDataSource {
    suspend fun saveStatement(month: String, accountId: String): AppResult<Unit>
}
