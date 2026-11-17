package com.eyther.lumbridge.app.home.data

import com.eyther.lumbridge.app.demo.data.DemoFeedDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoShoppingDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingSelectionDataSource
import com.eyther.lumbridge.app.demo.data.DemoTaskDataSource
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeTasksStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart

internal class HomeRetryFixture(
    private val failFirstTask: Boolean = true,
    private val blockSecondaryReads: Boolean = false,
) {
    private val store = DemoHouseholdStore(context = DemoHouseholdStore.households.first())
    val context = store.context
    var loads = 0
        private set

    private val secondaryGate = CompletableDeferred<Unit>()
    private val demoFeed = DemoFeedDataSource(store = store)
    private val feed =
        object : FeedDataSource by demoFeed {
            override fun stream(): Flow<AppResult<List<ArticleDomain>>> =
                demoFeed.stream().onStart { if (blockSecondaryReads) secondaryGate.await() }
        }
    private val demoShopping = DemoShoppingDataSource(store = store)
    private val shopping =
        object : ShoppingDataSource by demoShopping {
            override fun stream(): Flow<AppResult<ShoppingSnapshotDomain>> =
                demoShopping.stream().onStart { if (blockSecondaryReads) secondaryGate.await() }
        }

    fun releaseSecondaryReads() {
        secondaryGate.complete(Unit)
    }

    private val tasks = DemoTaskDataSource(store = store)
    private val failingTasks =
        object : TaskDataSource by tasks {
            override fun stream(): Flow<AppResult<List<TaskDomain>>> =
                if (loads++ == 0 && failFirstTask)
                    flowOf(AppResult.Failure(CommonError.NoConnection()))
                else tasks.stream()
        }

    fun viewModel(): HomeScreenViewModel =
        HomeScreenViewModel(
            getHomeTasksStreamUseCase =
                GetHomeTasksStreamUseCase(
                    getTasksStreamUseCase = GetTasksStreamUseCase(taskDataSource = failingTasks),
                    context = context,
                ),
            getHomeShoppingStreamUseCase =
                GetHomeShoppingStreamUseCase(
                    getShoppingSelectionStreamUseCase =
                        GetShoppingSelectionStreamUseCase(
                            shoppingSelectionDataSource =
                                DemoShoppingSelectionDataSource(store = store),
                        ),
                    getShoppingStreamUseCase =
                        GetShoppingStreamUseCase(
                            shoppingDataSource = shopping,
                        ),
                ),
            getFeedStreamUseCase = GetFeedStreamUseCase(feedDataSource = feed),
            markArticleReadUseCase = MarkArticleReadUseCase(feedDataSource = feed),
            context = context,
        )
}
