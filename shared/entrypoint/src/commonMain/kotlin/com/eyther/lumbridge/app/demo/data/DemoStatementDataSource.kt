package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource

class DemoStatementDataSource : StatementDataSource {

    override suspend fun saveStatement(month: String) = AppResult.Success(Unit)
}
