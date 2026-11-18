package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.SessionStartup
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LauncherHost() {
    val getLegalDocumentsUseCase: GetLegalDocumentsUseCase = koinInject()
    val uriHandler = LocalUriHandler.current
    val getSessionStreamUseCase: GetSessionStreamUseCase = koinInject()
    val authentication by getSessionStreamUseCase().collectAsState()
    val session = (authentication as? AuthenticationStateDomain.Authenticated)?.session
    val analyticsDataSource: AnalyticsDataSource = koinInject()
    val diagnosticsDataSource: DiagnosticsDataSource = koinInject()
    val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase = koinInject()
    val getAnalyticsPreferencesStreamUseCase: GetAnalyticsPreferencesStreamUseCase = koinInject()
    val analyticsPreferences by getAnalyticsPreferencesStreamUseCase().collectAsState()
    val signOut: SignOutUseCase = koinInject()
    val access: SupabaseAccess = koinInject()
    val source: HouseholdDataSource = koinInject()
    val restore: RestoreHouseholdUseCase = koinInject()
    val rememberSelection: RememberHouseholdUseCase = koinInject()
    val scope = rememberCoroutineScope()
    var context by remember(session?.userId) { mutableStateOf<HouseholdContextDomain?>(null) }
    var restoring by remember(session?.userId) { mutableStateOf(session != null) }
    LaunchedEffect(session?.userId) {
        val user = session ?: return@LaunchedEffect
        when (val result = restore(user.userId)) {
            is AppResult.Success ->
                result.value?.let {
                    context =
                        HouseholdContextDomain(
                            it.id,
                            user.userId,
                            it.name,
                            it.currency,
                            it.timeZone,
                        )
                }
            is AppResult.Failure -> Unit
        }
        restoring = false
    }
    val publicScreen =
        when {
            authentication == AuthenticationStateDomain.SignedOut ->
                AnalyticsScreenDomain.AUTHENTICATION
            else -> null
        }
    LaunchedEffect(publicScreen, analyticsPreferences.enabled) {
        if (publicScreen != null) {
            trackAnalyticsEventUseCase(
                event = AnalyticsEventDomain.ScreenViewed(screen = publicScreen),
            )
        }
    }
    when {
        authentication is AuthenticationStateDomain.Restoring ->
            SessionStartup(
                reconnecting = (authentication as AuthenticationStateDomain.Restoring).reconnecting,
            )
        authentication == AuthenticationStateDomain.SignedOut ->
            AuthenticationHost(
                onOpenLegalDocument = { document ->
                    uriHandler.openUri(getLegalDocumentsUseCase().url(document = document))
                },
            )
        restoring -> SessionStartup(reconnecting = false)
        else -> {
            val user = session ?: return
            val profileDataSource: ProfileDataSource = koinInject()
            key(user.userId) {
                PortalHost(
                    userId = user.userId,
                    context = context,
                    access = access,
                    householdDataSource = source,
                    profileDataSource = profileDataSource,
                    analyticsDataSource = analyticsDataSource,
                    diagnosticsDataSource = diagnosticsDataSource,
                    onSelect = { household ->
                        scope.launch {
                            rememberSelection(userId = user.userId, householdId = household.id)
                            context =
                                HouseholdContextDomain(
                                    id = household.id,
                                    userId = user.userId,
                                    name = household.name,
                                    currency = household.currency,
                                    timeZone = household.timeZone,
                                )
                        }
                    },
                    onSignOut = { scope.launch { signOut() } },
                )
            }
        }
    }
}
