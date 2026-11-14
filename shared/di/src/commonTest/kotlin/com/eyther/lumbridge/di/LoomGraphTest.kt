package com.eyther.lumbridge.di

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.data.local.datasource.CommonAnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.AuthenticationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class LoomGraphTest {

    private class AuthenticationStub : AuthenticationDataSource {

        override val configured: Boolean = true

        override fun stream(): StateFlow<AuthSessionDomain?> = MutableStateFlow(null)

        override suspend fun signIn(email: String, password: String): AppResult<Unit> =
            error("This graph test does not sign in")

        override suspend fun register(email: String, password: String): AppResult<Unit> =
            error("This graph test does not register accounts")

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

        override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
            AppResult.Success(emptyList())

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
            val authentication = graph.koin.get<AuthenticationScreenViewModelContract>()
            val household = graph.koin.get<HouseholdScreenViewModelContract>()
            try {
                assertIs<AuthenticationScreenViewModel>(authentication)
                assertIs<HouseholdScreenViewModel>(household)
                assertSame(graph.koin.get<GetHouseholdsUseCase>(), graph.koin.get<GetHouseholdsUseCase>())
            } finally {
                authentication.viewModelScope.cancel()
                household.viewModelScope.cancel()
            }
        } finally {
            graph.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun householdGraphsIsolateTheirSourcesAndKeepTheSharedAnalyticsChoice() {
        val firstContext = HouseholdContextDomain(
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
        val first = koinApplication {
            modules(
                householdModules(
                    context = firstContext,
                    access = access,
                    householdDataSource = households,
                    analyticsDataSource = analytics,
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
                ),
            )
        }
        try {
            assertSame(firstContext, first.koin.get<HouseholdContextDomain>())
            assertSame(secondContext, second.koin.get<HouseholdContextDomain>())
            assertSame(analytics, first.koin.get<AnalyticsDataSource>())
            assertSame(analytics, second.koin.get<AnalyticsDataSource>())
            assertSame(households, second.koin.get<HouseholdDataSource>())
            assertNotSame(first.koin.get<ShoppingDataSource>(), second.koin.get<ShoppingDataSource>())
            assertNotSame(first.koin.get<GetShoppingStreamUseCase>(), second.koin.get<GetShoppingStreamUseCase>())
        } finally {
            first.close()
            second.close()
        }
    }
}
