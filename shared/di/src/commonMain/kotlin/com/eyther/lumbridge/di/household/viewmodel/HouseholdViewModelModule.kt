package com.eyther.lumbridge.di.household.viewmodel

import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.SetAnalyticsConsentUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.RecurringExpensesScreenViewModel
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
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
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
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.SettingsScreenViewModel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class HouseholdViewModelModule {

    @KoinViewModel(binds = [ExpensesScreenViewModelContract::class])
    fun provideExpensesScreenViewModel(
        getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
        getMembersUseCase: GetMembersUseCase,
        addExpenseUseCase: AddExpenseUseCase,
        recordSettlementUseCase: RecordSettlementUseCase,
        context: HouseholdContextDomain,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): ExpensesScreenViewModelContract = ExpensesScreenViewModel(
        getExpenseLedgerStreamUseCase = getExpenseLedgerStreamUseCase,
        getMembersUseCase = getMembersUseCase,
        addExpenseUseCase = addExpenseUseCase,
        recordSettlementUseCase = recordSettlementUseCase,
        context = context,
        trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
    )

    @KoinViewModel(binds = [RecurringExpensesScreenViewModelContract::class])
    fun provideRecurringExpensesScreenViewModel(
        getRecurringExpensesStreamUseCase: GetRecurringExpensesStreamUseCase,
        controlRecurringExpenseUseCase: ControlRecurringExpenseUseCase,
        getMembersUseCase: GetMembersUseCase,
        context: HouseholdContextDomain,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): RecurringExpensesScreenViewModelContract = RecurringExpensesScreenViewModel(
        getRecurringExpensesStreamUseCase = getRecurringExpensesStreamUseCase,
        controlRecurringExpenseUseCase = controlRecurringExpenseUseCase,
        getMembersUseCase = getMembersUseCase,
        context = context,
        trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
    )

    @KoinViewModel(binds = [FeedScreenViewModelContract::class])
    fun provideFeedScreenViewModel(
        getFeedStreamUseCase: GetFeedStreamUseCase,
        followFeedUseCase: FollowFeedUseCase,
        markArticleReadUseCase: MarkArticleReadUseCase,
    ): FeedScreenViewModelContract = FeedScreenViewModel(
        getFeedStreamUseCase = getFeedStreamUseCase,
        followFeedUseCase = followFeedUseCase,
        markArticleReadUseCase = markArticleReadUseCase,
    )

    @KoinViewModel(binds = [HomeScreenViewModelContract::class])
    fun provideHomeScreenViewModel(
        getHomeOverviewStreamUseCase: GetHomeOverviewStreamUseCase,
    ): HomeScreenViewModelContract = HomeScreenViewModel(
        getHomeOverviewStreamUseCase = getHomeOverviewStreamUseCase,
    )

    @KoinViewModel(binds = [InsightsScreenViewModelContract::class])
    fun provideInsightsScreenViewModel(
        getMonthlyStatementStreamUseCase: GetMonthlyStatementStreamUseCase,
        saveStatementUseCase: SaveStatementUseCase,
        context: HouseholdContextDomain,
        exportStatementUseCase: ExportStatementUseCase,
    ): InsightsScreenViewModelContract = InsightsScreenViewModel(
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
    ): RemindersScreenViewModelContract = RemindersScreenViewModel(
        getRemindersStreamUseCase = getRemindersStreamUseCase,
        addReminderUseCase = addReminderUseCase,
        completeReminderUseCase = completeReminderUseCase,
        removeReminderUseCase = removeReminderUseCase,
    )

    @KoinViewModel(binds = [SettingsScreenViewModelContract::class])
    fun provideSettingsScreenViewModel(
        getSettingsStreamUseCase: GetSettingsStreamUseCase,
        setThemeUseCase: SetThemeUseCase,
        setNotificationsUseCase: SetNotificationsUseCase,
        getMembersUseCase: GetMembersUseCase,
        createInvitationUseCase: CreateInvitationUseCase,
        context: HouseholdContextDomain,
        getAnalyticsPreferencesStreamUseCase: GetAnalyticsPreferencesStreamUseCase,
        setAnalyticsConsentUseCase: SetAnalyticsConsentUseCase,
    ): SettingsScreenViewModelContract = SettingsScreenViewModel(
        getSettingsStreamUseCase = getSettingsStreamUseCase,
        setThemeUseCase = setThemeUseCase,
        setNotificationsUseCase = setNotificationsUseCase,
        getMembersUseCase = getMembersUseCase,
        createInvitationUseCase = createInvitationUseCase,
        context = context,
        getAnalyticsPreferencesStreamUseCase = getAnalyticsPreferencesStreamUseCase,
        setAnalyticsConsentUseCase = setAnalyticsConsentUseCase,
    )

    @KoinViewModel(binds = [ShoppingScreenViewModelContract::class])
    fun provideShoppingScreenViewModel(
        getShoppingStreamUseCase: GetShoppingStreamUseCase,
        addShoppingItemsUseCase: AddShoppingItemsUseCase,
        saveShoppingItemUseCase: SaveShoppingItemUseCase,
        saveShoppingListUseCase: SaveShoppingListUseCase,
        trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
    ): ShoppingScreenViewModelContract = ShoppingScreenViewModel(
        getShoppingStreamUseCase = getShoppingStreamUseCase,
        addShoppingItemsUseCase = addShoppingItemsUseCase,
        saveShoppingItemUseCase = saveShoppingItemUseCase,
        saveShoppingListUseCase = saveShoppingListUseCase,
        trackAnalyticsEventUseCase = trackAnalyticsEventUseCase,
    )
}
