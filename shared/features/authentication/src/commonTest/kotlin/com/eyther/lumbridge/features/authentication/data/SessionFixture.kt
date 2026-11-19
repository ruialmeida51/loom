package com.eyther.lumbridge.features.authentication.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.SessionScreenViewModel
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

internal class SessionFixture {
    val authentication = FakeAuthenticationDataSource()
    val home = HouseholdDomain("home", "Home", "EUR", "UTC")
    val trip = HouseholdDomain("trip", "Trip", "GBP", "Europe/London")
    val other = HouseholdDomain("other", "Other", "EUR", "UTC")
    val availableHouseholds =
        MutableStateFlow<AppResult<List<HouseholdDomain>>>(
            AppResult.Success(listOf(home, trip, other)),
        )
    val selected = mutableMapOf("you" to home.id, "another" to trip.id)
    var restoreCalls = 0
    var restoreResult: AppResult<List<HouseholdDomain>> =
        AppResult.Success(listOf(home, trip, other))
    var restoreGate: CompletableDeferred<AppResult<List<HouseholdDomain>>>? = null
    var selectionGate: CompletableDeferred<Unit>? = null
    var selectionResult: AppResult<Unit> = AppResult.Success(Unit)
    val startedSelections = mutableListOf<String?>()

    private val householdDataSource =
        object : HouseholdDataSource {
            override fun streamHouseholds(): Flow<AppResult<List<HouseholdDomain>>> =
                availableHouseholds

            override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> {
                restoreCalls++
                val gate = restoreGate
                return if (gate == null) restoreResult
                else withContext(NonCancellable) { gate.await() }
            }

            override suspend fun createHousehold(
                name: String,
                currency: String,
                timeZone: String,
            ): AppResult<HouseholdDomain> = error("Not used")

            override suspend fun joinHousehold(token: String): AppResult<Unit> = error("Not used")

            override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
                error("Not used")

            override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
                error("Not used")

            override suspend fun revokeInvitation(householdId: String): AppResult<Unit> =
                error("Not used")

            override suspend fun createInvitation(householdId: String): AppResult<String> =
                error("Not used")
        }
    private val selectionDataSource =
        object : HouseholdSelectionDataSource {
            override suspend fun getLastSelected(userId: String): AppResult<String?> =
                AppResult.Success(selected[userId])

            override suspend fun select(userId: String, householdId: String?): AppResult<Unit> {
                startedSelections += householdId
                val gate = selectionGate
                if (gate != null) withContext(NonCancellable) { gate.await() }
                if (selectionResult is AppResult.Success) {
                    if (householdId == null) selected.remove(userId)
                    else selected[userId] = householdId
                }
                return selectionResult
            }
        }

    fun viewModel(): SessionScreenViewModel =
        SessionScreenViewModel(
            getHouseholdsStreamUseCase =
                GetHouseholdsStreamUseCase(householdDataSource = householdDataSource),
            getSessionStreamUseCase = GetSessionStreamUseCase(dataSource = authentication),
            restoreHouseholdUseCase =
                RestoreHouseholdUseCase(
                    selectionDataSource = selectionDataSource,
                    getHouseholdsUseCase =
                        GetHouseholdsUseCase(householdDataSource = householdDataSource),
                ),
            rememberHouseholdUseCase =
                RememberHouseholdUseCase(selectionDataSource = selectionDataSource),
            signOutUseCase = SignOutUseCase(source = authentication),
        )
}
