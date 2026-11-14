package com.eyther.lumbridge.features.insights.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource

class SaveStatementUseCase(
    private val statementDataSource: StatementDataSource,
) {

    suspend operator fun invoke(month: String): AppResult<Unit> = statementDataSource.saveStatement(month)
}
