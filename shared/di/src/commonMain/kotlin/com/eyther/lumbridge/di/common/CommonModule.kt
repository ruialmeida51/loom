package com.eyther.lumbridge.di.common

import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.InitializeAnalyticsUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.SetAnalyticsConsentUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.legal.data.local.datasource.CommonLegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.datasource.LegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class CommonModule {

    @Single
    fun provideGetAnalyticsPreferencesStreamUseCase(
        analyticsDataSource: AnalyticsDataSource,
    ): GetAnalyticsPreferencesStreamUseCase = GetAnalyticsPreferencesStreamUseCase(
        analyticsDataSource = analyticsDataSource,
    )

    @Single
    fun provideInitializeAnalyticsUseCase(
        analyticsDataSource: AnalyticsDataSource,
    ): InitializeAnalyticsUseCase = InitializeAnalyticsUseCase(
        analyticsDataSource = analyticsDataSource,
    )

    @Single
    fun provideSetAnalyticsConsentUseCase(
        analyticsDataSource: AnalyticsDataSource,
    ): SetAnalyticsConsentUseCase = SetAnalyticsConsentUseCase(
        analyticsDataSource = analyticsDataSource,
    )

    @Single
    fun provideTrackAnalyticsEventUseCase(
        analyticsDataSource: AnalyticsDataSource,
    ): TrackAnalyticsEventUseCase = TrackAnalyticsEventUseCase(
        analyticsDataSource = analyticsDataSource,
    )

    @Single
    fun provideGetLegalDocumentsUseCase(
        legalDocumentsDataSource: LegalDocumentsDataSource,
    ): GetLegalDocumentsUseCase = GetLegalDocumentsUseCase(
        legalDocumentsDataSource = legalDocumentsDataSource,
    )

    @Single
    fun provideLegalDocumentsDataSource(): LegalDocumentsDataSource = CommonLegalDocumentsDataSource()
}
