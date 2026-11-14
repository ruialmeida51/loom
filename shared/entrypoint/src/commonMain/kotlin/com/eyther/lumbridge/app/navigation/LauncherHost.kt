package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.di.householdModule
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.screen.AuthenticationScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

@Composable
fun LauncherHost() {
    val getLegalDocumentsUseCase: GetLegalDocumentsUseCase = koinInject()
    val uriHandler = LocalUriHandler.current
    val getSessionStreamUseCase: GetSessionStreamUseCase = koinInject()
    val session by getSessionStreamUseCase().collectAsState()
    val analyticsDataSource: AnalyticsDataSource = koinInject()
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
    val publicScreen = when {
        session == null -> AnalyticsScreenDomain.AUTHENTICATION
        context == null && !restoring -> AnalyticsScreenDomain.HOUSEHOLDS
        else -> null
    }
    LaunchedEffect(publicScreen, analyticsPreferences.enabled) {
        if (publicScreen != null) {
            trackAnalyticsEventUseCase(event = AnalyticsEventDomain.ScreenViewed(screen = publicScreen))
        }
    }
    when {
        session == null ->
            AuthenticationScreen(
                viewModel = koinViewModel<AuthenticationScreenViewModelContract>(),
                onOpenLegalDocument = { document ->
                    uriHandler.openUri(getLegalDocumentsUseCase().url(document = document))
                },
            )
        restoring ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        context == null ->
            HouseholdScreen(
                koinViewModel<HouseholdScreenViewModelContract>(),
                onSelect = { household ->
                    val user = session ?: return@HouseholdScreen
                    scope.launch {
                        rememberSelection(user.userId, household.id)
                        context =
                            HouseholdContextDomain(
                                household.id,
                                user.userId,
                                household.name,
                                household.currency,
                                household.timeZone,
                            )
                    }
                },
                onSignOut = { scope.launch { signOut() } },
            )
        else -> {
            val selected = context!!
            key(selected.id, selected.userId) {
                val graph =
                    remember(selected) {
                        koinApplication {
                            modules(
                                householdModule(
                                    context = selected,
                                    access = access,
                                    householdSource = source,
                                    analyticsDataSource = analyticsDataSource,
                                ),
                            )
                        }
                    }
                DisposableEffect(graph) { onDispose { graph.close() } }
                KoinIsolatedContext(context = graph) {
                    HouseholdHost(
                        selected,
                        onSwitchHousehold = { context = null },
                        onSignOut = { scope.launch { signOut() } },
                    )
                }
            }
        }
    }
}
