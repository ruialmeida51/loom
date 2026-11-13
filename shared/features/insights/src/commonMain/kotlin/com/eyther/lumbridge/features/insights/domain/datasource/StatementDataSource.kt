package com.eyther.lumbridge.features.insights.domain.datasource

import com.eyther.lumbridge.core.domain.AppResult

interface StatementDataSource {
    suspend fun save(month: String): AppResult<Unit>
}
