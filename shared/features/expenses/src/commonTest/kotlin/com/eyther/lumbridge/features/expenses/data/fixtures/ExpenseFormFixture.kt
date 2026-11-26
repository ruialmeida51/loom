package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.datasource.ExchangeRateDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DownloadReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExchangeRateUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.PrepareReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseDetailsScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseFormScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseFormFixture {
    val receipts = ReceiptFixtureDataSource()
    var saveCalls = 0

    val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "UTC",
        )
    val account =
        ExpenseAccountDomain(
            id = "home",
            name = "Everyday",
            currency = "EUR",
            memberIds = setOf("you", "alex"),
        )
    val members =
        listOf(MemberDomain("you", "You", "owner"), MemberDomain("alex", "Alex", "member"))
    var accountResult = CompletableDeferred<AppResult<ExpenseAccountDomain>>()
    var membersResult = CompletableDeferred<AppResult<List<MemberDomain>>>()
    var saveResult: AppResult<Unit> = AppResult.Success(Unit)
    var referenceAccountRequests = 0
    var referenceMemberRequests = 0
    var accountRequests = 0
    var memberRequests = 0
    var saved: ExpenseDomain? = null
    var loadedExpense: ExpenseDomain? = null
    var deleteResult: AppResult<Unit> = AppResult.Success(Unit)
    val deletedExpenses = mutableListOf<ExpenseDomain>()

    val accounts: ExpenseAccountDataSource =
        object : ExpenseAccountDataSource {
            override fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>> = error("Not used")

            override fun streamSelection(): StateFlow<String> = MutableStateFlow("home")

            override suspend fun getReferenceAccount(id: String): AppResult<ExpenseAccountDomain> {
                referenceAccountRequests++
                return accountResult.await()
            }

            override suspend fun getAccount(id: String): AppResult<ExpenseAccountDomain> {
                accountRequests++
                return accountResult.await()
            }

            override suspend fun selectAccount(id: String): AppResult<Unit> = error("Not used")

            override suspend fun saveAccount(
                account: ExpenseAccountDomain,
            ): AppResult<ExpenseAccountDomain> = error("Not used")

            override suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit> =
                error("Not used")

            override suspend fun saveGuest(
                accountId: String,
                guest: ExpenseGuestDomain,
            ): AppResult<ExpenseAccountDomain> = error("Not used")

            override suspend fun setIcon(accountId: String, icon: ExpenseAccountIconDomain?): AppResult<ExpenseAccountDomain> =

                error("Not used")


            override suspend fun removeGuest(
                accountId: String,
                guest: ExpenseGuestDomain,
            ): AppResult<ExpenseAccountDomain> = error("Not used")
        }
    val household: HouseholdDataSource =
        object : HouseholdDataSource {
            override suspend fun getMemberReferences(
                householdId: String,
            ): AppResult<List<MemberDomain>> {
                referenceMemberRequests++
                return membersResult.await()
            }

            override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> {
                memberRequests++
                return membersResult.await()
            }

            override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> =
                error("Not used")

            override suspend fun createHousehold(
                name: String,
                currency: String,
                timeZone: String,
            ): AppResult<HouseholdDomain> = error("Not used")

            override suspend fun joinHousehold(token: String): AppResult<Unit> = error("Not used")

            override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
                error("Not used")

            override suspend fun revokeInvitation(householdId: String): AppResult<Unit> =
                error("Not used")

            override suspend fun createInvitation(householdId: String): AppResult<String> =
                error("Not used")
        }
    private val expenses =
        object : ExpenseDataSource {
            override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> {
                saveCalls++
                saved = expense
                return saveResult
            }

            override suspend fun getSettlement(
                id: String,
                accountId: String,
            ): AppResult<SettlementDomain> = error("Not used")

            override suspend fun getExpense(
                id: String,
                accountId: String,
            ): AppResult<ExpenseDomain> =
                loadedExpense?.let { AppResult.Success(it) }
                    ?: AppResult.Failure(CommonError.NotFound())

            override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
                error("Not used")

            override fun streamSettlements(
                accountId: String,
            ): Flow<AppResult<List<SettlementDomain>>> = error("Not used")

            override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> {
                deletedExpenses += expense
                return deleteResult
            }

            override suspend fun saveSettlement(
                settlement: SettlementDomain,
            ): AppResult<SettlementDomain> = AppResult.Success(settlement)

            override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> =
                AppResult.Success(Unit)

            override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
                error("Not used")
        }
    private val analytics =
        object : AnalyticsDataSource {
            override fun stream(): StateFlow<AnalyticsPreferencesDomain> =
                MutableStateFlow(AnalyticsPreferencesDomain())

            override suspend fun initialize(): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun setConsent(enabled: Boolean): AppResult<Unit> =
                AppResult.Success(Unit)

            override suspend fun track(event: AnalyticsEventDomain): AppResult<Unit> =
                AppResult.Success(Unit)
        }

    /** One pound buys €1.1546 unless a test says otherwise. */
    var rateResult: AppResult<String> = AppResult.Success("1.1546")
    val rates: ExchangeRateDataSource =
        object : ExchangeRateDataSource {
            override suspend fun latestRate(from: String, to: String): AppResult<String> = rateResult
        }

    fun viewModel(): ExpenseFormScreenViewModel =
        ExpenseFormScreenViewModel(
            getExpenseReceiptUseCase =
                GetExpenseReceiptUseCase(expenseReceiptDataSource = receipts),
            prepareReceiptPhotoUseCase =
                PrepareReceiptPhotoUseCase(expenseReceiptDataSource = receipts),
            saveExpenseReceiptUseCase =
                SaveExpenseReceiptUseCase(expenseReceiptDataSource = receipts),
            params = ExpenseFormScreenViewModelContract.Params(accountId = account.id),
            getExpenseFormUseCase =
                GetExpenseFormUseCase(
                    expenseAccountDataSource = accounts,
                    getExpenseUseCase = GetExpenseUseCase(expenseDataSource = expenses),
                    householdDataSource = household,
                    context = context,
                ),
            saveExpenseUseCase =
                SaveExpenseUseCase(
                    expenseDataSource = expenses,
                    expenseAccountDataSource = accounts,
                    members = GetMembersUseCase(householdDataSource = household),
                    context = context,
                ),
            context = context,
            getExchangeRateUseCase = GetExchangeRateUseCase(exchangeRateDataSource = rates),
            trackAnalyticsEventUseCase =
                TrackAnalyticsEventUseCase(analyticsDataSource = analytics),
        )

    fun detailsViewModel(): ExpenseDetailsScreenViewModel =
        ExpenseDetailsScreenViewModel(
            downloadReceiptPhotoUseCase =
                DownloadReceiptPhotoUseCase(
                    expenseReceiptDataSource = receipts,
                ),
            getExpenseReceiptUseCase =
                GetExpenseReceiptUseCase(expenseReceiptDataSource = receipts),
            prepareReceiptPhotoUseCase =
                PrepareReceiptPhotoUseCase(expenseReceiptDataSource = receipts),
            saveExpenseReceiptUseCase =
                SaveExpenseReceiptUseCase(expenseReceiptDataSource = receipts),
            removeExpenseReceiptUseCase =
                RemoveExpenseReceiptUseCase(expenseReceiptDataSource = receipts),
            params =
                ExpenseDetailsScreenViewModelContract.Params(
                    accountId = account.id,
                    expenseId = "expense",
                ),
            getExpenseFormUseCase =
                GetExpenseFormUseCase(
                    expenseAccountDataSource = accounts,
                    getExpenseUseCase = GetExpenseUseCase(expenseDataSource = expenses),
                    householdDataSource = household,
                    context = context,
                ),
            deleteExpenseUseCase = DeleteExpenseUseCase(expenseDataSource = expenses),
            context = context,
        )
}
