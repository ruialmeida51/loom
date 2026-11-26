package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.features.account.data.network.datasource.CommonPaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.settings.data.network.datasource.CommonSettingsDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AccountDataSourceModule(
    private val householdDataSource: HouseholdDataSource,
    private val profileDataSource: ProfileDataSource,
    private val analyticsDataSource: AnalyticsDataSource,
    private val diagnosticsDataSource: DiagnosticsDataSource,
    private val userId: String,
    private val access: SupabaseAccess,
) {
    @Single fun provideHouseholdDataSource(): HouseholdDataSource = householdDataSource

    @Single fun provideProfileDataSource(): ProfileDataSource = profileDataSource

    @Single fun provideDiagnosticsDataSource(): DiagnosticsDataSource = diagnosticsDataSource

    @Single fun provideAnalyticsDataSource(): AnalyticsDataSource = analyticsDataSource

    @Single
    fun providePaymentHandlesDataSource(): PaymentHandlesDataSource =
        CommonPaymentHandlesDataSource(access = access, userId = userId)

    @Single
    fun provideSettingsDataSource(): SettingsDataSource =
        CommonSettingsDataSource(access = access, userId = userId)
}
