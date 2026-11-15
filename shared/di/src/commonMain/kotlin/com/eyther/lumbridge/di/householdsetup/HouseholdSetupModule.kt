package com.eyther.lumbridge.di.householdsetup

import org.koin.core.annotation.Module

@Module(includes = [HouseholdSetupUseCaseModule::class, HouseholdSetupViewModelModule::class])
class HouseholdSetupModule
