package com.eyther.lumbridge.di.household.viewmodel

import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ResolveExpenseSplitUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SelectExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseAccountFormScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseDetailsScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseFormScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpenseSplitViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.RecurringExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseSplitViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RemoveFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.UpdateFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedSubscriptionsScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsScreenViewModelContract
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeTasksStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeInvitationUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdDetailsScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.presentation.viewmodel.InsightsScreenViewModel
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.DeleteShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveCompletedShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SelectShoppingListUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.CompleteTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.RemoveTaskUseCase
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.TasksScreenViewModel
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class HouseholdViewModelModule {

    @KoinViewModel(binds = [ExpensesScreenViewModelContract::class])
    fun provideExpensesScreenViewModel(
        getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
        getExpenseAccountsStreamUseCase: GetExpenseAccountsStreamUseCase,
        selectExpenseAccountUseCase: SelectExpenseAccountUseCase,
        saveExpenseAccountUseCase: SaveExpenseAccountUseCase,
        deleteExpenseAccountUseCase: DeleteExpenseAccountUseCase,
        recordSettlementUseCase: RecordSettlementUseCase,
        getMembersStreamUseCase: GetMembersStreamUseCase,
        context: HouseholdContextDomain,
    ): ExpensesScreenViewModelContract =
        ExpensesScreenViewModel(
            getExpenseLedgerStreamUseCase = getExpenseLedgerStreamUseCase,
            getExpenseAccountsStreamUseCase = getExpenseAccountsStreamUseCase,
            selectExpenseAccountUseCase = selectExpenseAccountUseCase,
            saveExpenseAccountUseCase = saveExpenseAccountUseCase,
            deleteExpenseAccountUseCase = deleteExpenseAccountUseCase,
            recordSettlementUseCase = recordSettlementUseCase,
            getMembersStreamUseCase = getMembersStreamUseCase,
            context = context,
        )

    @KoinViewModel(binds = [RecurringExpensesScreenViewModelContract::class])
    fun provideRecurringExpensesScreenViewModel(
        getRecurringExpensesStreamUseCase: GetRecurringExpensesStreamUseCase,
        controlRecurringExpenseUseCase: ControlRecurringExpenseUseCase,
        getMembersStreamUseCase: GetMembersStreamUseCase,
        context: HouseholdContextDomain,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): RecurringExpensesScreenViewModelContract =
        RecurringExpensesScreenViewModel(
            getRecurringExpensesStreamUseCase = getRecurringExpensesStreamUseCase,
            controlRecurringExpenseUseCase = controlRecurringExpenseUseCase,
            getMembersStreamUseCase = getMembersStreamUseCase,
            context = context,
            trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
        )

    @KoinViewModel(binds = [FeedScreenViewModelContract::class])
    fun provideFeedScreenViewModel(
        context: HouseholdContextDomain,
        getFeedPageUseCase: GetFeedPageUseCase,
        getFeedSubscriptionsStreamUseCase: GetFeedSubscriptionsStreamUseCase,
        getStarterFeedsUseCase: GetStarterFeedsUseCase,
        refreshFeedsUseCase: RefreshFeedsUseCase,
        followFeedUseCase: FollowFeedUseCase,
        markArticleReadUseCase: MarkArticleReadUseCase,
    ): FeedScreenViewModelContract =
        FeedScreenViewModel(
            timeZone = context.timeZone,
            getFeedPageUseCase = getFeedPageUseCase,
            getFeedSubscriptionsStreamUseCase = getFeedSubscriptionsStreamUseCase,
            getStarterFeedsUseCase = getStarterFeedsUseCase,
            refreshFeedsUseCase = refreshFeedsUseCase,
            followFeedUseCase = followFeedUseCase,
            markArticleReadUseCase = markArticleReadUseCase,
        )

    @KoinViewModel(binds = [HomeScreenViewModelContract::class])
    fun provideHomeScreenViewModel(
        getHomeTasksStreamUseCase: GetHomeTasksStreamUseCase,
        getHomeShoppingStreamUseCase: GetHomeShoppingStreamUseCase,
        getFeedStreamUseCase: GetFeedStreamUseCase,
        markArticleReadUseCase: MarkArticleReadUseCase,
        context: HouseholdContextDomain,
    ): HomeScreenViewModelContract =
        HomeScreenViewModel(
            getHomeTasksStreamUseCase = getHomeTasksStreamUseCase,
            getHomeShoppingStreamUseCase = getHomeShoppingStreamUseCase,
            getFeedStreamUseCase = getFeedStreamUseCase,
            markArticleReadUseCase = markArticleReadUseCase,
            context = context,
        )

    @KoinViewModel(binds = [InsightsScreenViewModelContract::class])
    fun provideInsightsScreenViewModel(
        getMonthlyStatementStreamUseCase: GetMonthlyStatementStreamUseCase,
        saveStatementUseCase: SaveStatementUseCase,
        context: HouseholdContextDomain,
        exportStatementUseCase: ExportStatementUseCase,
    ): InsightsScreenViewModelContract =
        InsightsScreenViewModel(
            getMonthlyStatementStreamUseCase = getMonthlyStatementStreamUseCase,
            saveStatementUseCase = saveStatementUseCase,
            context = context,
            exportStatementUseCase = exportStatementUseCase,
        )

    @KoinViewModel(binds = [TasksScreenViewModelContract::class])
    fun provideTasksScreenViewModel(
        getTasksStreamUseCase: GetTasksStreamUseCase,
        addTaskUseCase: AddTaskUseCase,
        completeTaskUseCase: CompleteTaskUseCase,
        removeTaskUseCase: RemoveTaskUseCase,
        context: HouseholdContextDomain,
    ): TasksScreenViewModelContract =
        TasksScreenViewModel(
            context = context,
            getTasksStreamUseCase = getTasksStreamUseCase,
            addTaskUseCase = addTaskUseCase,
            completeTaskUseCase = completeTaskUseCase,
            removeTaskUseCase = removeTaskUseCase,
        )

    @KoinViewModel(binds = [HouseholdDetailsScreenViewModelContract::class])
    fun provideHouseholdDetailsScreenViewModel(
        context: HouseholdContextDomain,
        getMembersStreamUseCase: GetMembersStreamUseCase,
        createInvitationUseCase: CreateInvitationUseCase,
        revokeInvitationUseCase: RevokeInvitationUseCase,
    ): HouseholdDetailsScreenViewModelContract =
        HouseholdDetailsScreenViewModel(
            context = context,
            getMembersStreamUseCase = getMembersStreamUseCase,
            createInvitationUseCase = createInvitationUseCase,
            revokeInvitationUseCase = revokeInvitationUseCase,
        )

    @KoinViewModel(binds = [ShoppingScreenViewModelContract::class])
    fun provideShoppingScreenViewModel(
        getShoppingStreamUseCase: GetShoppingStreamUseCase,
        getShoppingSelectionStreamUseCase: GetShoppingSelectionStreamUseCase,
        selectShoppingListUseCase: SelectShoppingListUseCase,
        addShoppingItemsUseCase: AddShoppingItemsUseCase,
        saveShoppingItemUseCase: SaveShoppingItemUseCase,
        saveShoppingListUseCase: SaveShoppingListUseCase,
        deleteShoppingListUseCase: DeleteShoppingListUseCase,
        removeCompletedShoppingItemsUseCase: RemoveCompletedShoppingItemsUseCase,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): ShoppingScreenViewModelContract =
        ShoppingScreenViewModel(
            getShoppingStreamUseCase = getShoppingStreamUseCase,
            getShoppingSelectionStreamUseCase = getShoppingSelectionStreamUseCase,
            selectShoppingListUseCase = selectShoppingListUseCase,
            addShoppingItemsUseCase = addShoppingItemsUseCase,
            saveShoppingItemUseCase = saveShoppingItemUseCase,
            saveShoppingListUseCase = saveShoppingListUseCase,
            deleteShoppingListUseCase = deleteShoppingListUseCase,
            removeCompletedShoppingItemsUseCase = removeCompletedShoppingItemsUseCase,
            trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
        )

    @KoinViewModel(binds = [ExpenseFormScreenViewModelContract::class])
    fun provideExpenseFormScreenViewModel(
        @InjectedParam params: ExpenseFormScreenViewModelContract.Params,
        getExpenseFormUseCase: GetExpenseFormUseCase,
        saveExpenseUseCase: SaveExpenseUseCase,
        context: HouseholdContextDomain,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): ExpenseFormScreenViewModelContract =
        ExpenseFormScreenViewModel(
            params = params,
            getExpenseFormUseCase = getExpenseFormUseCase,
            saveExpenseUseCase = saveExpenseUseCase,
            context = context,
            trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
        )

    @KoinViewModel(binds = [ExpenseDetailsScreenViewModelContract::class])
    fun provideExpenseDetailsScreenViewModel(
        @InjectedParam params: ExpenseDetailsScreenViewModelContract.Params,
        getExpenseFormUseCase: GetExpenseFormUseCase,
        deleteExpenseUseCase: DeleteExpenseUseCase,
        context: HouseholdContextDomain,
    ): ExpenseDetailsScreenViewModelContract =
        ExpenseDetailsScreenViewModel(
            params = params,
            getExpenseFormUseCase = getExpenseFormUseCase,
            deleteExpenseUseCase = deleteExpenseUseCase,
            context = context,
        )

    @KoinViewModel(binds = [ExpenseAccountFormScreenViewModelContract::class])
    fun provideExpenseAccountFormScreenViewModel(
        @InjectedParam params: ExpenseAccountFormScreenViewModelContract.Params,
        getExpenseAccountFormUseCase: GetExpenseAccountFormUseCase,
        saveExpenseAccountUseCase: SaveExpenseAccountUseCase,
        selectExpenseAccountUseCase: SelectExpenseAccountUseCase,
        context: HouseholdContextDomain,
    ): ExpenseAccountFormScreenViewModelContract =
        ExpenseAccountFormScreenViewModel(
            params = params,
            getExpenseAccountFormUseCase = getExpenseAccountFormUseCase,
            saveExpenseAccountUseCase = saveExpenseAccountUseCase,
            selectExpenseAccountUseCase = selectExpenseAccountUseCase,
            context = context,
        )

    @KoinViewModel(binds = [ExpenseSplitViewModelContract::class])
    fun provideExpenseSplitViewModel(
        @InjectedParam params: ExpenseSplitViewModelContract.Params,
        resolveExpenseSplitUseCase: ResolveExpenseSplitUseCase,
    ): ExpenseSplitViewModelContract =
        ExpenseSplitViewModel(
            params = params,
            resolveExpenseSplitUseCase = resolveExpenseSplitUseCase,
        )

    @KoinViewModel(binds = [FeedSubscriptionsScreenViewModelContract::class])
    fun provideFeedSubscriptionsScreenViewModel(
        getFeedSubscriptionsStreamUseCase: GetFeedSubscriptionsStreamUseCase,
        followFeedUseCase: FollowFeedUseCase,
        refreshFeedSubscriptionUseCase: RefreshFeedSubscriptionUseCase,
        updateFeedSubscriptionUseCase: UpdateFeedSubscriptionUseCase,
        removeFeedSubscriptionUseCase: RemoveFeedSubscriptionUseCase,
    ): FeedSubscriptionsScreenViewModelContract =
        FeedSubscriptionsScreenViewModel(
            getFeedSubscriptionsStreamUseCase = getFeedSubscriptionsStreamUseCase,
            followFeedUseCase = followFeedUseCase,
            refreshFeedSubscriptionUseCase = refreshFeedSubscriptionUseCase,
            updateFeedSubscriptionUseCase = updateFeedSubscriptionUseCase,
            removeFeedSubscriptionUseCase = removeFeedSubscriptionUseCase,
        )
}
