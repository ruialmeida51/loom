package com.eyther.lumbridge.features.insights.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource

class SaveStatementUseCase(private val source: StatementDataSource) {
    suspend operator fun invoke(month: String) = source.save(month)
}
