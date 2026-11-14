package com.eyther.lumbridge.app.di

import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.InitializeAnalyticsUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.SetAnalyticsConsentUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun analyticsUseCasesModule(): Module = module {
    single { GetAnalyticsPreferencesStreamUseCase(analyticsDataSource = get()) }
    single { InitializeAnalyticsUseCase(analyticsDataSource = get()) }
    single { SetAnalyticsConsentUseCase(analyticsDataSource = get()) }
    single { TrackAnalyticsEventUseCase(analyticsDataSource = get()) }
}
