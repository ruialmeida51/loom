package com.eyther.lumbridge.features.insights.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.errors.InsightsError
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class ExportStatementTest {

    @Test
    fun aFailedExportKeepsItsTypedError() = runTest {
        val error = InsightsError.ExportFailed()
        val source = object : StatementExportDataSource {
            override suspend fun export(filename: String, content: String, title: String): AppResult<Unit> =
                AppResult.Failure(error)
        }

        val result = ExportStatementUseCase(source)("statement.csv", "month,total", "Statement")

        assertSame(error, assertIs<AppResult.Failure>(result).error)
    }
}
