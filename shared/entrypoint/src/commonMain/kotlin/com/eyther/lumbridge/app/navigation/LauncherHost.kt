package com.eyther.lumbridge.app.navigation

import androidx.compose.runtime.*
import com.eyther.lumbridge.app.di.householdModule
import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.authentication.domain.usecase.ObserveSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.screen.AuthenticationScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import kotlinx.coroutines.launch
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

@Composable
fun LauncherHost() {
    val sessionUseCase: ObserveSessionUseCase = koinInject()
    val session by sessionUseCase().collectAsState()
    val signOut: SignOutUseCase = koinInject()
    val access: SupabaseAccess = koinInject()
    val householdSource: HouseholdDataSource = koinInject()
    val scope = rememberCoroutineScope()
    var context by
        remember(session?.userId, session?.demo) {
            mutableStateOf(if (session?.demo == true) DemoHousehold.context else null)
        }
    when {
        session == null ->
            AuthenticationScreen(koinViewModel<AuthenticationScreenViewModelContract>())
        context == null ->
            HouseholdScreen(
                koinViewModel<HouseholdScreenViewModelContract>(),
                onSelect = { household ->
                    context =
                        HouseholdContextDomain(
                            household.id,
                            session!!.userId,
                            household.name,
                            household.currency,
                            household.timeZone,
                            false,
                        )
                },
                onSignOut = { scope.launch { signOut() } },
            )
        else -> {
            val selected = context!!
            key(selected.id, selected.userId, selected.demo) {
                val graph =
                    remember(selected) {
                        koinApplication {
                            modules(householdModule(selected, access, householdSource))
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
