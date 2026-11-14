package com.eyther.lumbridge.app.home.data

import com.eyther.lumbridge.app.demo.data.DemoExpenseDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoReminderDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingDataSource
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class HomeRetryFixture {
    private val store = DemoHouseholdStore(context = DemoHouseholdStore.households.first())
    val context = store.context
    var loads = 0
        private set

    private val shoppingSource = DemoShoppingDataSource(store = store)
    private val expensesSource = DemoExpenseDataSource(store = store)
    private val failingShopping =
        object : ShoppingDataSource by shoppingSource {
            override fun stream(): Flow<AppResult<ShoppingSnapshotDomain>> =
                if (loads++ == 0) flowOf(AppResult.Failure(CommonError.NoConnection()))
                else shoppingSource.stream()
        }

    fun viewModel(): HomeScreenViewModel =
        HomeScreenViewModel(
            getHomeOverviewStreamUseCase =
                GetHomeOverviewStreamUseCase(
                    getShoppingStreamUseCase =
                        GetShoppingStreamUseCase(shoppingDataSource = failingShopping),
                    getRemindersStreamUseCase =
                        GetRemindersStreamUseCase(
                            reminderDataSource = DemoReminderDataSource(store = store),
                        ),
                    getExpenseLedgerStreamUseCase =
                        GetExpenseLedgerStreamUseCase(
                            getExpensesStreamUseCase =
                                GetExpensesStreamUseCase(expenseDataSource = expensesSource),
                            getSettlementsStreamUseCase =
                                GetSettlementsStreamUseCase(expenseDataSource = expensesSource),
                        ),
                    context = context,
                ),
            context = context,
        )
}
