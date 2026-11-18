package com.eyther.lumbridge.di.application.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.createLoomSupabaseClient
import com.eyther.lumbridge.features.account.data.network.datasource.CommonProfileDataSource
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.analytics.data.local.datasource.CommonAnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.authentication.data.local.SecureCodeVerifierCache
import com.eyther.lumbridge.features.authentication.data.local.SecureSessionManager
import com.eyther.lumbridge.features.authentication.data.network.config.configureLoomAuthentication
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.data.platform.NativeAuthenticationBridge
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.diagnostics.data.local.datasource.CommonDiagnosticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.household.data.local.CommonHouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.data.network.datasource.CommonHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.settings.data.notifications.CommonPushDeviceDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ApplicationDataSourceModule {
    @Single
    fun provideDiagnosticsDataSource(): DiagnosticsDataSource = CommonDiagnosticsDataSource()

    @Single
    fun provideProfileDataSource(access: SupabaseAccess): ProfileDataSource =
        CommonProfileDataSource(access = access)

    @Single
    fun provideSupabaseAccess(): SupabaseAccess =
        SupabaseAccess(
            client =
                createLoomSupabaseClient(
                    configureAuthentication = {
                        configureLoomAuthentication(
                            sessionStorage = SecureSessionManager(),
                            verifierCache = SecureCodeVerifierCache(),
                        )
                    },
                ),
        )

    @Single fun provideAnalyticsDataSource(): AnalyticsDataSource = CommonAnalyticsDataSource()

    @Single
    fun providePushDeviceDataSource(
        access: SupabaseAccess,
    ): PushDeviceDataSource =
        CommonPushDeviceDataSource(
            access = access,
        )

    @Single
    fun provideAuthenticationDataSource(
        access: SupabaseAccess,
        pushDevices: PushDeviceDataSource,
    ): AuthenticationDataSource =
        CommonAuthenticationDataSource(
            access = access,
            pushDevices = pushDevices,
            callbackEvents = NativeAuthenticationBridge.stream(),
        )

    @Single
    fun provideHouseholdDataSource(
        access: SupabaseAccess,
        profileDataSource: ProfileDataSource,
    ): HouseholdDataSource =
        CommonHouseholdDataSource(
            access = access,
            profileDataSource = profileDataSource,
        )

    @Single
    fun provideHouseholdSelectionDataSource(): HouseholdSelectionDataSource =
        CommonHouseholdSelectionDataSource()
}
