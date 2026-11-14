package com.eyther.lumbridge.features.insights.data.export

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.errors.InsightsError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Native share dialogs require the UI dispatcher. */
class CommonStatementExportDataSource : StatementExportDataSource {

    override suspend fun export(filename: String, content: String, title: String): AppResult<Unit> {
        return withContext(Dispatchers.Main.immediate) {
            try {
                PlatformExport.shareCsv(filename = filename, content = content, title = title)
                AppResult.Success(Unit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AppError) {
                AppResult.Failure(error)
            } catch (error: Exception) {
                AppResult.Failure(InsightsError.ExportFailed(cause = error))
            }
        }
    }
}
