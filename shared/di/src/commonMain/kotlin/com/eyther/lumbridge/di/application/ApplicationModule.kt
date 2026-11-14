package com.eyther.lumbridge.di.application

import com.eyther.lumbridge.di.application.datasource.ApplicationDataSourceModule
import com.eyther.lumbridge.di.application.usecase.ApplicationUseCaseModule
import com.eyther.lumbridge.di.application.viewmodel.ApplicationViewModelModule
import com.eyther.lumbridge.di.common.CommonModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        CommonModule::class,
        ApplicationDataSourceModule::class,
        ApplicationUseCaseModule::class,
        ApplicationViewModelModule::class,
    ],
)
class ApplicationModule
