package com.eyther.lumbridge.features.legal.domain.datasource

import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentsDomain

interface LegalDocumentsDataSource {

    fun getDocuments(): LegalDocumentsDomain
}
