package com.eyther.lumbridge.di.household

import com.eyther.lumbridge.di.common.CommonModule
import com.eyther.lumbridge.di.household.usecase.HouseholdUseCaseModule
import com.eyther.lumbridge.di.household.viewmodel.HouseholdViewModelModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        CommonModule::class,
        HouseholdUseCaseModule::class,
        HouseholdViewModelModule::class,
    ],
)
class HouseholdModule
