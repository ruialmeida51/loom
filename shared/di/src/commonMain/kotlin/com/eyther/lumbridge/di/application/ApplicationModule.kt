package com.eyther.lumbridge.di.application

import com.eyther.lumbridge.di.application.datasource.ApplicationDataSourceModule
import com.eyther.lumbridge.di.application.usecase.ApplicationUseCaseModule
import com.eyther.lumbridge.di.application.viewmodel.ApplicationViewModelModule
import com.eyther.lumbridge.di.common.CommonModule
import com.eyther.lumbridge.di.consent.ConsentModule
import com.eyther.lumbridge.di.householdsetup.HouseholdSetupModule
import org.koin.core.annotation.Module

@Module(
    includes =
        [
            HouseholdSetupModule::class,
            CommonModule::class,
            ConsentModule::class,
            ApplicationDataSourceModule::class,
            ApplicationUseCaseModule::class,
            ApplicationViewModelModule::class,
        ],
)
class ApplicationModule
