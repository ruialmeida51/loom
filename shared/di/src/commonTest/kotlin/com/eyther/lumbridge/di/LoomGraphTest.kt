package com.eyther.lumbridge.di

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.data.local.datasource.CommonAnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.EmailVerificationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.LoginScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.RegisterScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.CreateHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.JoinHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class LoomGraphTest {

    private class AuthenticationStub : AuthenticationDataSource {

        override val configured: Boolean = true

        override fun stream(): StateFlow<AuthenticationStateDomain> =
            MutableStateFlow(AuthenticationStateDomain.SignedOut)

        override suspend fun signInWithGoogle(): AppResult<Unit> = AppResult.Success(Unit)

        override fun streamGoogleSignInResults(): Flow<AppResult<Unit>> = emptyFlow()

        override suspend fun signIn(email: String, password: String): AppResult<Unit> =
            error("This graph test does not sign in")

        override suspend fun register(email: String, password: String): AppResult<Unit> =
            error("This graph test does not register accounts")

        override suspend fun verifyEmail(email: String, code: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun resendVerificationEmail(email: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun sendSignInCode(email: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun signOut(): AppResult<Unit> = error("This graph test does not sign out")
    }

    private class HouseholdStub : HouseholdDataSource {

        override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> =
            AppResult.Success(emptyList())

        override suspend fun createHousehold(
            name: String,
            currency: String,
            timeZone: String,
        ): AppResult<HouseholdDomain> = error("This graph test does not create households")

        override suspend fun joinHousehold(token: String): AppResult<Unit> =
            error("This graph test does not join households")

        override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
            flow {
                emit(getMembers(householdId = householdId))
            }

        override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
            AppResult.Success(emptyList())

        override suspend fun revokeInvitation(householdId: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun createInvitation(householdId: String): AppResult<String> =
            error("This graph test does not create invitations")
    }

    @Test
    fun annotatedApplicationProvidersResolveViewModelContractsWithoutServerAccess() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val graph = koinApplication {
            modules(applicationModules())
            modules(
                module {
                    single<AuthenticationDataSource> { AuthenticationStub() }
                    single<HouseholdDataSource> { HouseholdStub() }
                },
            )
        }
        try {
            val authentication = graph.koin.get<LoginScreenViewModelContract>()
            val registration = graph.koin.get<RegisterScreenViewModelContract>()
            val verification =
                graph.koin.get<EmailVerificationScreenViewModelContract> {
                    parametersOf(
                        EmailVerificationScreenViewModelContract.Params(
                            email = "member@example.com",
                            codeRequested = false,
                        ),
                    )
                }
            val household = graph.koin.get<HouseholdScreenViewModelContract>()
            val createHousehold = graph.koin.get<CreateHouseholdScreenViewModelContract>()
            val joinHousehold = graph.koin.get<JoinHouseholdScreenViewModelContract>()
            try {
                assertIs<LoginScreenViewModel>(authentication)
                assertIs<RegisterScreenViewModel>(registration)
                assertIs<EmailVerificationScreenViewModel>(verification)
                assertIs<HouseholdScreenViewModel>(household)
                assertIs<CreateHouseholdScreenViewModel>(createHousehold)
                assertIs<JoinHouseholdScreenViewModel>(joinHousehold)
                assertSame(
                    graph.koin.get<GetHouseholdsUseCase>(),
                    graph.koin.get<GetHouseholdsUseCase>(),
                )
            } finally {
                authentication.viewModelScope.cancel()
                registration.viewModelScope.cancel()
                verification.viewModelScope.cancel()
                household.viewModelScope.cancel()
                createHousehold.viewModelScope.cancel()
                joinHousehold.viewModelScope.cancel()
            }
        } finally {
            graph.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun householdGraphsIsolateTheirSourcesAndKeepTheSharedAnalyticsChoice() {
        val firstContext =
            HouseholdContextDomain(
                id = "first-household",
                userId = "member",
                name = "First household",
                currency = "EUR",
                timeZone = "UTC",
            )
        val secondContext = firstContext.copy(id = "second-household", name = "Second household")
        val analytics = CommonAnalyticsDataSource()
        val households = HouseholdStub()
        val access = SupabaseAccess(client = null)
        val settings =
            object : SettingsDataSource {
                override fun stream() = MutableStateFlow(SettingsDomain())

                override suspend fun setTheme(value: String): AppResult<Unit> =
                    AppResult.Success(Unit)

                override suspend fun setNotifications(enabled: Boolean): AppResult<Unit> =
                    AppResult.Success(Unit)
            }
        val first = koinApplication {
            modules(
                householdModules(
                    context = firstContext,
                    access = access,
                    householdDataSource = households,
                    analyticsDataSource = analytics,
                    settingsDataSource = settings,
                ),
            )
        }
        val second = koinApplication {
            modules(
                householdModules(
                    context = secondContext,
                    access = access,
                    householdDataSource = households,
                    analyticsDataSource = analytics,
                    settingsDataSource = settings,
                ),
            )
        }
        try {
            assertSame(firstContext, first.koin.get<HouseholdContextDomain>())
            assertSame(secondContext, second.koin.get<HouseholdContextDomain>())
            assertSame(analytics, first.koin.get<AnalyticsDataSource>())
            assertSame(settings, first.koin.get<SettingsDataSource>())
            assertSame(settings, second.koin.get<SettingsDataSource>())
            assertSame(analytics, second.koin.get<AnalyticsDataSource>())
            assertSame(households, second.koin.get<HouseholdDataSource>())
            assertNotSame(
                first.koin.get<ShoppingDataSource>(),
                second.koin.get<ShoppingDataSource>(),
            )
            assertNotSame(
                first.koin.get<GetShoppingStreamUseCase>(),
                second.koin.get<GetShoppingStreamUseCase>(),
            )
        } finally {
            first.close()
            second.close()
        }
    }
}
