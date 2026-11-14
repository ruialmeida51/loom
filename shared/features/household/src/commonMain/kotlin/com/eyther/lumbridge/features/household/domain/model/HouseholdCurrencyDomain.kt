package com.eyther.lumbridge.features.household.domain.model

/** A household chooses one supported two-decimal currency for its ledger. */
enum class HouseholdCurrencyDomain {

    EUR,
    GBP,
    USD,
    CAD,
    AUD;

    companion object {
        val supportedCodes: Set<String> = entries.map { it.name }.toSet()
    }
}
