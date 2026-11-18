package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.di.common.CommonModule
import com.eyther.lumbridge.di.consent.ConsentModule
import com.eyther.lumbridge.di.householdsetup.HouseholdSetupModule
import com.eyther.lumbridge.di.notifications.NotificationsModule
import org.koin.core.annotation.Module

@Module(
    includes =
        [
            HouseholdSetupModule::class,
            CommonModule::class,
            NotificationsModule::class,
            ConsentModule::class,
            AccountUseCaseModule::class,
            AccountViewModelModule::class,
        ],
)
class AccountModule
