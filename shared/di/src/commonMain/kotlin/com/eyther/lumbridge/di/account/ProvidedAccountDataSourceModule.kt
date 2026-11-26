package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ProvidedAccountDataSourceModule(
    private val householdDataSource: HouseholdDataSource,
    private val profileDataSource: ProfileDataSource,
    private val analyticsDataSource: AnalyticsDataSource,
    private val diagnosticsDataSource: DiagnosticsDataSource,
    private val settingsDataSource: SettingsDataSource,
    private val paymentHandlesDataSource: PaymentHandlesDataSource,
) {
    @Single fun providePaymentHandlesDataSource(): PaymentHandlesDataSource = paymentHandlesDataSource

    @Single fun provideHouseholdDataSource(): HouseholdDataSource = householdDataSource

    @Single fun provideProfileDataSource(): ProfileDataSource = profileDataSource

    @Single fun provideDiagnosticsDataSource(): DiagnosticsDataSource = diagnosticsDataSource

    @Single fun provideAnalyticsDataSource(): AnalyticsDataSource = analyticsDataSource

    @Single fun provideSettingsDataSource(): SettingsDataSource = settingsDataSource
}
