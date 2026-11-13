package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.di.householdModule
import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.authentication.domain.usecase.ObserveSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.screen.AuthenticationScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.screen.HouseholdScreen
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import kotlinx.coroutines.launch
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

@Composable
fun LauncherHost() {
    val observeSession: ObserveSessionUseCase = koinInject()
    val session by observeSession().collectAsState()
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
    when {
        session == null ->
            AuthenticationScreen(koinViewModel<AuthenticationScreenViewModelContract>())
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
                        koinApplication { modules(householdModule(selected, access, source)) }
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
