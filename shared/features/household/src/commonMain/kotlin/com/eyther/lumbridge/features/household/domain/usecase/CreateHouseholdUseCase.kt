package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.datetime.TimeZone

class CreateHouseholdUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(name: String, currency: String): AppResult<HouseholdDomain> {
        if (name.trim().length !in 2..60)
            return AppResult.Failure(
                HouseholdError.InvalidName(),
            )
        if (currency !in HouseholdCurrencyDomain.supportedCodes)
            return AppResult.Failure(HouseholdError.UnsupportedCurrency())
        return householdDataSource.createHousehold(name.trim(), currency, TimeZone.currentSystemDefault().id)
    }
}
