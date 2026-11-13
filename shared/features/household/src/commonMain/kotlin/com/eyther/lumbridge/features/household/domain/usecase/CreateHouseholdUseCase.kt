package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.datetime.TimeZone

class CreateHouseholdUseCase(private val source: HouseholdDataSource) {
    suspend operator fun invoke(name: String, currency: String): AppResult<HouseholdDomain> {
        if (name.trim().length !in 2..60)
            return AppResult.Failure(
                "household.message_give_your_household_a_name_between_2_and_60",
            )
        if (currency !in HouseholdCurrencyDomain.supportedCodes)
            return AppResult.Failure("household.message_choose_a_supported_currency")
        return source.create(name.trim(), currency, TimeZone.currentSystemDefault().id)
    }
}
