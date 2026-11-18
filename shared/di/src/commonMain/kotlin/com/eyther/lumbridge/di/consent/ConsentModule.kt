package com.eyther.lumbridge.di.consent

import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.settings.data.local.datasource.CommonUsageDataConsentDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetUsageDataPreferencesStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.InitializeUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SaveUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.UsageDataConsentScreenViewModel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentScreenViewModelContract
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConsentModule {

    @Single
    fun provideUsageDataConsentDataSource(
        analytics: AnalyticsDataSource,
        diagnostics: DiagnosticsDataSource,
    ): UsageDataConsentDataSource =
        CommonUsageDataConsentDataSource(analytics = analytics, diagnostics = diagnostics)

    @Single
    fun provideGetUsageDataPreferencesStreamUseCase(
        dataSource: UsageDataConsentDataSource,
    ): GetUsageDataPreferencesStreamUseCase =
        GetUsageDataPreferencesStreamUseCase(dataSource = dataSource)

    @Single
    fun provideInitializeUsageDataConsentUseCase(
        dataSource: UsageDataConsentDataSource,
    ): InitializeUsageDataConsentUseCase =
        InitializeUsageDataConsentUseCase(dataSource = dataSource)

    @Single
    fun provideSaveUsageDataConsentUseCase(
        dataSource: UsageDataConsentDataSource,
    ): SaveUsageDataConsentUseCase = SaveUsageDataConsentUseCase(dataSource = dataSource)

    @KoinViewModel(binds = [UsageDataConsentScreenViewModelContract::class])
    fun provideUsageDataConsentScreenViewModel(
        getPreferences: GetUsageDataPreferencesStreamUseCase,
        initializeConsent: InitializeUsageDataConsentUseCase,
        saveConsent: SaveUsageDataConsentUseCase,
    ): UsageDataConsentScreenViewModelContract =
        UsageDataConsentScreenViewModel(
            getPreferences = getPreferences,
            initializeConsent = initializeConsent,
            saveConsent = saveConsent,
        )
}
