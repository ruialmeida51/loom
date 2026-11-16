package com.eyther.lumbridge.app.home.data

import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoReminderDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingSelectionDataSource
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class HomeRetryFixture {
    private val store = DemoHouseholdStore(context = DemoHouseholdStore.households.first())
    val context = store.context
    var loads = 0
        private set

    private val reminders = DemoReminderDataSource(store = store)
    private val failingReminders =
        object : ReminderDataSource by reminders {
            override fun stream(): Flow<AppResult<List<ReminderDomain>>> =
                if (loads++ == 0) flowOf(AppResult.Failure(CommonError.NoConnection()))
                else reminders.stream()
        }

    fun viewModel(): HomeScreenViewModel =
        HomeScreenViewModel(
            getHomeOverviewStreamUseCase =
                GetHomeOverviewStreamUseCase(
                    getRemindersStreamUseCase =
                        GetRemindersStreamUseCase(reminderDataSource = failingReminders),
                    context = context,
                    getShoppingSelectionStreamUseCase =
                        GetShoppingSelectionStreamUseCase(
                            shoppingSelectionDataSource =
                                DemoShoppingSelectionDataSource(store = store),
                        ),
                    getShoppingStreamUseCase =
                        GetShoppingStreamUseCase(
                            shoppingDataSource = DemoShoppingDataSource(store = store),
                        ),
                ),
            context = context,
        )
}
