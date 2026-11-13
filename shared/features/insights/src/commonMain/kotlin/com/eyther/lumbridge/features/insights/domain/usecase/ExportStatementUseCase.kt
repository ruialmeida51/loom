package com.eyther.lumbridge.features.insights.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource

class ExportStatementUseCase(
    private val statementExportDataSource: StatementExportDataSource,
) {

    suspend operator fun invoke(filename: String, content: String, title: String): AppResult<Unit> =
        statementExportDataSource.export(filename = filename, content = content, title = title)
}
