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
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
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
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.RemindersScreenViewModel
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
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
        getFeedStreamUseCase: GetFeedStreamUseCase,
        followFeedUseCase: FollowFeedUseCase,
        markArticleReadUseCase: MarkArticleReadUseCase,
    ): FeedScreenViewModelContract =
        FeedScreenViewModel(
            getFeedStreamUseCase = getFeedStreamUseCase,
            followFeedUseCase = followFeedUseCase,
            markArticleReadUseCase = markArticleReadUseCase,
        )

    @KoinViewModel(binds = [HomeScreenViewModelContract::class])
    fun provideHomeScreenViewModel(
        getHomeOverviewStreamUseCase: GetHomeOverviewStreamUseCase,
        context: HouseholdContextDomain,
    ): HomeScreenViewModelContract =
        HomeScreenViewModel(
            getHomeOverviewStreamUseCase = getHomeOverviewStreamUseCase,
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

    @KoinViewModel(binds = [RemindersScreenViewModelContract::class])
    fun provideRemindersScreenViewModel(
        getRemindersStreamUseCase: GetRemindersStreamUseCase,
        addReminderUseCase: AddReminderUseCase,
        completeReminderUseCase: CompleteReminderUseCase,
        removeReminderUseCase: RemoveReminderUseCase,
        context: HouseholdContextDomain,
    ): RemindersScreenViewModelContract =
        RemindersScreenViewModel(
            context = context,
            getRemindersStreamUseCase = getRemindersStreamUseCase,
            addReminderUseCase = addReminderUseCase,
            completeReminderUseCase = completeReminderUseCase,
            removeReminderUseCase = removeReminderUseCase,
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
        getMembersStreamUseCase: GetMembersStreamUseCase,
        context: HouseholdContextDomain,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): ExpenseFormScreenViewModelContract =
        ExpenseFormScreenViewModel(
            params = params,
            getExpenseFormUseCase = getExpenseFormUseCase,
            saveExpenseUseCase = saveExpenseUseCase,
            getMembersStreamUseCase = getMembersStreamUseCase,
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
}
