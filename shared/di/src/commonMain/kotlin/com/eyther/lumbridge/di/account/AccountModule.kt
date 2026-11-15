package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.di.common.CommonModule
import com.eyther.lumbridge.di.householdsetup.HouseholdSetupModule
import org.koin.core.annotation.Module

@Module(
    includes =
        [
            HouseholdSetupModule::class,
            CommonModule::class,
            AccountUseCaseModule::class,
            AccountViewModelModule::class,
        ],
)
class AccountModule
