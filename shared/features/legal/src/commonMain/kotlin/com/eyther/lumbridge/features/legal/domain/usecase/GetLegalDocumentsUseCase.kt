package com.eyther.lumbridge.features.legal.domain.usecase

import com.eyther.lumbridge.features.legal.domain.datasource.LegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentsDomain

class GetLegalDocumentsUseCase(
    private val legalDocumentsDataSource: LegalDocumentsDataSource,
) {

    operator fun invoke(): LegalDocumentsDomain = legalDocumentsDataSource.getDocuments()
}
