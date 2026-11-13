package com.eyther.lumbridge.features.insights.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult

interface StatementExportDataSource {

    suspend fun export(filename: String, content: String, title: String): AppResult<Unit>
}
