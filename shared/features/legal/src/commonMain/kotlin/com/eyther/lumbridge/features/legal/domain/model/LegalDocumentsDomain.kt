package com.eyther.lumbridge.features.legal.domain.model

data class LegalDocumentsDomain(
    val privacyPolicyUrl: String,
    val termsUrl: String,
    val accountDeletionUrl: String,
    val version: String,
) {

    fun url(document: LegalDocumentDomain): String = when (document) {
        LegalDocumentDomain.PRIVACY_POLICY -> privacyPolicyUrl
        LegalDocumentDomain.TERMS -> termsUrl
        LegalDocumentDomain.ACCOUNT_DELETION -> accountDeletionUrl
    }
}
