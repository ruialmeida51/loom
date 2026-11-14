package com.eyther.lumbridge.features.legal.data.local.datasource

import com.eyther.lumbridge.features.legal.data.config.LegalConfiguration
import com.eyther.lumbridge.features.legal.domain.datasource.LegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentsDomain

class CommonLegalDocumentsDataSource : LegalDocumentsDataSource {

    override fun getDocuments(): LegalDocumentsDomain = LegalDocumentsDomain(
        privacyPolicyUrl = "${LegalConfiguration.siteUrl}/privacy/",
        termsUrl = "${LegalConfiguration.siteUrl}/terms/",
        accountDeletionUrl = "${LegalConfiguration.siteUrl}/delete-account/",
        version = LegalConfiguration.policyVersion,
    )
}
