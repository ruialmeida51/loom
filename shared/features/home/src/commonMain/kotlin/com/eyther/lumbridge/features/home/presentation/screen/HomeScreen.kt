package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.IllustratedEmptyState
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.RefreshSkeletonContent
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.presentation.components.FeedPreview
import com.eyther.lumbridge.features.feed.presentation.components.FeedPreviewLoading
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_all_news
import com.eyther.lumbridge.features.home.generated.resources.home_all_tasks
import com.eyther.lumbridge.features.home.generated.resources.home_choose_sources
import com.eyther.lumbridge.features.home.generated.resources.home_news
import com.eyther.lumbridge.features.home.generated.resources.home_news_empty
import com.eyther.lumbridge.features.home.generated.resources.home_nothing_due
import com.eyther.lumbridge.features.home.generated.resources.home_shopping
import com.eyther.lumbridge.features.home.generated.resources.home_shopping_empty
import com.eyther.lumbridge.features.home.generated.resources.home_shopping_view_all
import com.eyther.lumbridge.features.home.generated.resources.home_tasks
import com.eyther.lumbridge.features.home.generated.resources.ui_try_again
import com.eyther.lumbridge.features.home.presentation.components.HomeQuickActions
import com.eyther.lumbridge.features.home.presentation.components.HomeRefreshLoading
import com.eyther.lumbridge.features.home.presentation.components.HomeShoppingLoading
import com.eyther.lumbridge.features.home.presentation.components.HomeTaskRow
import com.eyther.lumbridge.features.home.presentation.components.HomeTasksLoading
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingPreview
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModelContract,
    onShopping: () -> Unit,
    onTasks: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTask: () -> Unit,
    onFeed: () -> Unit,
    onOpenStory: (String) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRetry,
        coverContentOnRefresh = false,
    ) {
        InnerHomeScreen(
            tasks = state.tasks,
            shoppingItems = state.shoppingItems,
            articles = state.articles,
            tasksLoading = state.tasksLoading,
            tasksRefreshing = state.tasksRefreshing,
            shoppingLoading = state.shoppingLoading,
            shoppingRefreshing = state.shoppingRefreshing,
            feedLoading = state.feedLoading,
            feedRefreshing = state.feedRefreshing,
            hasTasks = state.taskItems != null,
            hasFeed = state.feedItems != null,
            hasShopping = state.shoppingPreview != null,
            tasksError = state.tasksError,
            shoppingError = state.shoppingError,
            feedError = state.feedError,
            onRetry = viewModel::onRetry,
            onDismissTasksError = viewModel::onTasksErrorDismissed,
            onDismissShoppingError = viewModel::onShoppingErrorDismissed,
            onDismissFeedError = viewModel::onFeedErrorDismissed,
            onTasks = onTasks,
            onShopping = onShopping,
            onAddExpense = onAddExpense,
            onAddTask = onAddTask,
            onFeed = onFeed,
            onOpenStory = { article ->
                viewModel.onArticleOpened(id = article.id)
                onOpenStory(article.url)
            },
        )
    }
}

@Composable
private fun InnerHomeScreen(
    tasks: ImmutableList<HomeTaskUi>,
    shoppingItems: ImmutableList<ShoppingItemDomain>,
    articles: ImmutableList<ArticlePreviewUi>,
    tasksLoading: Boolean,
    tasksRefreshing: Boolean,
    shoppingLoading: Boolean,
    shoppingRefreshing: Boolean,
    feedLoading: Boolean,
    feedRefreshing: Boolean,
    hasTasks: Boolean,
    hasFeed: Boolean,
    hasShopping: Boolean,
    tasksError: TextResource?,
    shoppingError: TextResource?,
    feedError: TextResource?,
    onRetry: () -> Unit,
    onDismissTasksError: () -> Unit,
    onDismissShoppingError: () -> Unit,
    onDismissFeedError: () -> Unit,
    onShopping: () -> Unit,
    onTasks: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTask: () -> Unit,
    onFeed: () -> Unit,
    onOpenStory: (ArticlePreviewUi) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("home_content"),
        contentPadding = rootContentPadding(horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.section),
    ) {
        item(key = "quick_actions") {
            Column(Modifier.padding(horizontal = LoomSpacing.content)) {
                HomeQuickActions(onAddExpense = onAddExpense, onAddTask = onAddTask)
            }
        }
        item(key = "tasks") {
            RefreshSkeletonContent(
                isRefreshing = tasksRefreshing,
                skeleton = { HomeRefreshLoading() },
            ) {
                Column(
                    Modifier.padding(horizontal = LoomSpacing.content),
                    verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
                ) {
                    SectionTitle(
                        title = stringResource(Res.string.home_tasks),
                        action = stringResource(Res.string.home_all_tasks),
                        onAction = onTasks,
                    )
                    if (tasksLoading) HomeTasksLoading()
                    else if (tasks.isNotEmpty())
                        GroupedSurface {
                            tasks.forEachIndexed { index, task ->
                                HomeTaskRow(task = task, onClick = onTasks)
                                if (index < tasks.lastIndex)
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                            }
                        }
                    else if (hasTasks && tasksError == null)
                        IllustratedEmptyState(
                            title = stringResource(Res.string.home_nothing_due),
                            glyph = Glyph.CHECK,
                            modifier = Modifier.testTag("home_tasks_empty"),
                        )
                    tasksError?.let {
                        HomeSectionError(
                            message = it,
                            onDismiss = if (hasTasks) onDismissTasksError else null,
                            onRetry = onRetry,
                        )
                    }
                }
            }
        }
        if (shoppingLoading || hasShopping || shoppingError != null)
            item(key = "shopping_preview") {
                RefreshSkeletonContent(
                    isRefreshing = shoppingRefreshing,
                    skeleton = { HomeRefreshLoading() },
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
                        modifier =
                            Modifier.padding(horizontal = LoomSpacing.content)
                                .testTag("home_shopping_preview"),
                    ) {
                        SectionTitle(
                            title = stringResource(Res.string.home_shopping),
                            action = stringResource(Res.string.home_shopping_view_all),
                            onAction = onShopping,
                        )
                        if (shoppingLoading) HomeShoppingLoading()
                        else if (shoppingItems.isNotEmpty())
                            ShoppingPreview(items = shoppingItems, onOpen = onShopping)
                        else if (hasShopping && shoppingError == null)
                            IllustratedEmptyState(
                                title = stringResource(Res.string.home_shopping_empty),
                                glyph = Glyph.SHOPPING,
                            )
                        shoppingError?.let {
                            HomeSectionError(
                                message = it,
                                onDismiss = if (hasShopping) onDismissShoppingError else null,
                                onRetry = onRetry,
                            )
                        }
                    }
                }
            }
        item(key = "news") {
            RefreshSkeletonContent(
                isRefreshing = feedRefreshing,
                skeleton = {
                    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.content)) {
                        SectionTitle(
                            title = stringResource(Res.string.home_news),
                            action = stringResource(Res.string.home_all_news),
                            onAction = onFeed,
                            modifier = Modifier.padding(horizontal = LoomSpacing.content),
                        )
                        FeedPreviewLoading()
                    }
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.content)) {
                    SectionTitle(
                        title = stringResource(Res.string.home_news),
                        action = stringResource(Res.string.home_all_news),
                        onAction = onFeed,
                        modifier = Modifier.padding(horizontal = LoomSpacing.content),
                    )
                    if (feedLoading) FeedPreviewLoading()
                    else if (articles.isNotEmpty())
                        FeedPreview(articles = articles, onOpen = onOpenStory)
                    else if (hasFeed && feedError == null)
                        Column(Modifier.padding(horizontal = LoomSpacing.content)) {
                            GroupedSurface {
                                NavigationRow(
                                    title = stringResource(Res.string.home_choose_sources),
                                    description = stringResource(Res.string.home_news_empty),
                                    onClick = onFeed,
                                )
                            }
                        }
                    feedError?.let {
                        Column(Modifier.padding(horizontal = LoomSpacing.content)) {
                            HomeSectionError(
                                message = it,
                                onDismiss = if (hasFeed) onDismissFeedError else null,
                                onRetry = onRetry,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionError(message: TextResource, onDismiss: (() -> Unit)?, onRetry: () -> Unit) {
    ErrorNotice(message = message, onDismiss = onDismiss)
    OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.ui_try_again)) }
}
