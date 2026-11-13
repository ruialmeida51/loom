package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource

class DemoStatementDataSource : StatementDataSource {
    override suspend fun save(month: String) = AppResult.Success(Unit)
}
