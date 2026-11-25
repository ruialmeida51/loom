package com.eyther.lumbridge.di.household.usecase

import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.MemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DownloadReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetMemberAccountPositionsUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSelectedExpenseAccountStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.PrepareReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ResolveExpenseSplitUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SelectExpenseAccountUseCase
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RemoveFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.UpdateFeedSubscriptionUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeBalanceStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeTasksStreamUseCase
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.ChangeHouseholdMemberUseCase
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.DeleteHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdManagementStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.domain.usecase.ReplaceHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeInvitationUseCase
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.DeleteShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveCompletedShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SelectShoppingListUseCase
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.CompleteTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.RemoveTaskUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class HouseholdUseCaseModule {
    @Single
    fun provideGetHouseholdManagementStreamUseCase(
        householdManagementDataSource: HouseholdManagementDataSource,
    ): GetHouseholdManagementStreamUseCase =
        GetHouseholdManagementStreamUseCase(
            householdManagementDataSource = householdManagementDataSource,
        )

    @Single
    fun provideReplaceHouseholdInvitationUseCase(
        householdManagementDataSource: HouseholdManagementDataSource,
    ): ReplaceHouseholdInvitationUseCase =
        ReplaceHouseholdInvitationUseCase(
            householdManagementDataSource = householdManagementDataSource,
        )

    @Single
    fun provideRevokeHouseholdInvitationUseCase(
        householdManagementDataSource: HouseholdManagementDataSource,
    ): RevokeHouseholdInvitationUseCase =
        RevokeHouseholdInvitationUseCase(
            householdManagementDataSource = householdManagementDataSource,
        )

    @Single
    fun provideChangeHouseholdMemberUseCase(
        householdManagementDataSource: HouseholdManagementDataSource,
    ): ChangeHouseholdMemberUseCase =
        ChangeHouseholdMemberUseCase(householdManagementDataSource = householdManagementDataSource)

    @Single
    fun provideDeleteHouseholdUseCase(
        householdManagementDataSource: HouseholdManagementDataSource,
    ): DeleteHouseholdUseCase =
        DeleteHouseholdUseCase(householdManagementDataSource = householdManagementDataSource)

    @Single
    fun provideGetMemberAccountPositionsUseCase(
        memberAccountPositionDataSource: MemberAccountPositionDataSource,
    ): GetMemberAccountPositionsUseCase =
        GetMemberAccountPositionsUseCase(
            memberAccountPositionDataSource = memberAccountPositionDataSource,
        )

    @Single
    fun provideGetRecurringExpenseFormUseCase(
        recurringExpenseDataSource: RecurringExpenseDataSource,
        expenseAccountDataSource: ExpenseAccountDataSource,
        householdDataSource: HouseholdDataSource,
        context: HouseholdContextDomain,
    ): GetRecurringExpenseFormUseCase =
        GetRecurringExpenseFormUseCase(
            recurringExpenseDataSource = recurringExpenseDataSource,
            expenseAccountDataSource = expenseAccountDataSource,
            householdDataSource = householdDataSource,
            context = context,
        )

    @Single
    fun provideSaveRecurringExpenseUseCase(
        recurringExpenseDataSource: RecurringExpenseDataSource,
    ): SaveRecurringExpenseUseCase =
        SaveRecurringExpenseUseCase(recurringExpenseDataSource = recurringExpenseDataSource)

    @Single
    fun provideDeleteRecurringExpenseUseCase(
        recurringExpenseDataSource: RecurringExpenseDataSource,
    ): DeleteRecurringExpenseUseCase =
        DeleteRecurringExpenseUseCase(recurringExpenseDataSource = recurringExpenseDataSource)

    @Single
    fun provideGetMembersUseCase(householdDataSource: HouseholdDataSource): GetMembersUseCase =
        GetMembersUseCase(householdDataSource = householdDataSource)

    @Single
    fun provideSaveExpenseUseCase(
        expenseDataSource: ExpenseDataSource,
        expenseAccountDataSource: ExpenseAccountDataSource,
        members: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): SaveExpenseUseCase =
        SaveExpenseUseCase(
            expenseDataSource = expenseDataSource,
            expenseAccountDataSource = expenseAccountDataSource,
            members = members,
            context = context,
        )

    @Single
    fun provideControlRecurringExpenseUseCase(
        recurringExpenseDataSource: RecurringExpenseDataSource,
    ): ControlRecurringExpenseUseCase =
        ControlRecurringExpenseUseCase(
            recurringExpenseDataSource = recurringExpenseDataSource,
        )

    @Single
    fun provideGetExpenseLedgerStreamUseCase(
        getSelectedExpenseAccountStreamUseCase: GetSelectedExpenseAccountStreamUseCase,
        getExpensesStreamUseCase: GetExpensesStreamUseCase,
        getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
    ): GetExpenseLedgerStreamUseCase =
        GetExpenseLedgerStreamUseCase(
            getSelectedExpenseAccountStreamUseCase = getSelectedExpenseAccountStreamUseCase,
            getExpensesStreamUseCase = getExpensesStreamUseCase,
            getSettlementsStreamUseCase = getSettlementsStreamUseCase,
        )

    @Single
    fun provideGetExpensesStreamUseCase(
        expenseDataSource: ExpenseDataSource,
    ): GetExpensesStreamUseCase =
        GetExpensesStreamUseCase(
            expenseDataSource = expenseDataSource,
        )

    @Single
    fun provideGetRecurringExpensesStreamUseCase(
        recurringExpenseDataSource: RecurringExpenseDataSource,
        getSelectedExpenseAccountStreamUseCase: GetSelectedExpenseAccountStreamUseCase,
    ): GetRecurringExpensesStreamUseCase =
        GetRecurringExpensesStreamUseCase(
            recurringExpenseDataSource = recurringExpenseDataSource,
            getSelectedExpenseAccountStreamUseCase = getSelectedExpenseAccountStreamUseCase,
        )

    @Single
    fun provideGetSettlementsStreamUseCase(
        expenseDataSource: ExpenseDataSource,
    ): GetSettlementsStreamUseCase =
        GetSettlementsStreamUseCase(
            expenseDataSource = expenseDataSource,
        )

    @Single
    fun provideRecordSettlementUseCase(
        expenseDataSource: ExpenseDataSource,
        expenseAccountDataSource: ExpenseAccountDataSource,
        members: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): RecordSettlementUseCase =
        RecordSettlementUseCase(
            expenseDataSource = expenseDataSource,
            expenseAccountDataSource = expenseAccountDataSource,
            members = members,
            context = context,
        )

    @Single
    fun provideFollowFeedUseCase(
        feedDataSource: FeedDataSource,
    ): FollowFeedUseCase =
        FollowFeedUseCase(
            feedDataSource = feedDataSource,
        )

    @Single
    fun provideGetFeedPageUseCase(feedDataSource: FeedDataSource): GetFeedPageUseCase =
        GetFeedPageUseCase(feedDataSource = feedDataSource)

    @Single
    fun provideGetFeedStreamUseCase(
        feedDataSource: FeedDataSource,
    ): GetFeedStreamUseCase =
        GetFeedStreamUseCase(
            feedDataSource = feedDataSource,
        )

    @Single
    fun provideMarkArticleReadUseCase(
        feedDataSource: FeedDataSource,
    ): MarkArticleReadUseCase =
        MarkArticleReadUseCase(
            feedDataSource = feedDataSource,
        )

    @Single
    fun provideGetHomeTasksStreamUseCase(
        getTasksStreamUseCase: GetTasksStreamUseCase,
        context: HouseholdContextDomain,
    ): GetHomeTasksStreamUseCase =
        GetHomeTasksStreamUseCase(
            getTasksStreamUseCase = getTasksStreamUseCase,
            context = context,
        )

    @Single
    fun provideGetHomeBalanceStreamUseCase(
        getExpensesStreamUseCase: GetExpensesStreamUseCase,
        getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
        context: HouseholdContextDomain,
    ): GetHomeBalanceStreamUseCase =
        GetHomeBalanceStreamUseCase(
            getExpensesStreamUseCase = getExpensesStreamUseCase,
            getSettlementsStreamUseCase = getSettlementsStreamUseCase,
            context = context,
        )

    @Single
    fun provideGetHomeShoppingStreamUseCase(
        getShoppingStreamUseCase: GetShoppingStreamUseCase,
        getShoppingSelectionStreamUseCase: GetShoppingSelectionStreamUseCase,
    ): GetHomeShoppingStreamUseCase =
        GetHomeShoppingStreamUseCase(
            getShoppingStreamUseCase = getShoppingStreamUseCase,
            getShoppingSelectionStreamUseCase = getShoppingSelectionStreamUseCase,
        )

    @Single
    fun provideCreateInvitationUseCase(
        householdDataSource: HouseholdDataSource,
    ): CreateInvitationUseCase =
        CreateInvitationUseCase(
            householdDataSource = householdDataSource,
        )

    @Single
    fun provideGetMembersStreamUseCase(
        householdDataSource: HouseholdDataSource,
    ): GetMembersStreamUseCase =
        GetMembersStreamUseCase(
            householdDataSource = householdDataSource,
        )

    @Single
    fun provideExportStatementUseCase(
        statementExportDataSource: StatementExportDataSource,
    ): ExportStatementUseCase =
        ExportStatementUseCase(
            statementExportDataSource = statementExportDataSource,
        )

    @Single
    fun provideGetMonthlyStatementStreamUseCase(
        getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
        context: HouseholdContextDomain,
    ): GetMonthlyStatementStreamUseCase =
        GetMonthlyStatementStreamUseCase(
            getExpenseLedgerStreamUseCase = getExpenseLedgerStreamUseCase,
            context = context,
        )

    @Single
    fun provideSaveStatementUseCase(
        statementDataSource: StatementDataSource,
    ): SaveStatementUseCase =
        SaveStatementUseCase(
            statementDataSource = statementDataSource,
        )

    @Single
    fun provideAddTaskUseCase(
        taskDataSource: TaskDataSource,
        context: HouseholdContextDomain,
    ): AddTaskUseCase =
        AddTaskUseCase(
            taskDataSource = taskDataSource,
            context = context,
        )

    @Single
    fun provideCompleteTaskUseCase(
        taskDataSource: TaskDataSource,
    ): CompleteTaskUseCase =
        CompleteTaskUseCase(
            taskDataSource = taskDataSource,
        )

    @Single
    fun provideGetTasksStreamUseCase(
        taskDataSource: TaskDataSource,
    ): GetTasksStreamUseCase =
        GetTasksStreamUseCase(
            taskDataSource = taskDataSource,
        )

    @Single
    fun provideRemoveTaskUseCase(
        taskDataSource: TaskDataSource,
    ): RemoveTaskUseCase =
        RemoveTaskUseCase(
            taskDataSource = taskDataSource,
        )

    @Single
    fun provideGetSettingsStreamUseCase(
        settingsDataSource: SettingsDataSource,
    ): GetSettingsStreamUseCase =
        GetSettingsStreamUseCase(
            settingsDataSource = settingsDataSource,
        )

    @Single
    fun provideSetNotificationsUseCase(
        settingsDataSource: SettingsDataSource,
    ): SetNotificationsUseCase =
        SetNotificationsUseCase(
            settingsDataSource = settingsDataSource,
        )

    @Single
    fun provideSetThemeUseCase(
        settingsDataSource: SettingsDataSource,
    ): SetThemeUseCase =
        SetThemeUseCase(
            settingsDataSource = settingsDataSource,
        )

    @Single
    fun provideAddShoppingItemsUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): AddShoppingItemsUseCase =
        AddShoppingItemsUseCase(
            shoppingDataSource = shoppingDataSource,
        )

    @Single
    fun provideGetShoppingStreamUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): GetShoppingStreamUseCase =
        GetShoppingStreamUseCase(
            shoppingDataSource = shoppingDataSource,
        )

    @Single
    fun provideSaveShoppingItemUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): SaveShoppingItemUseCase =
        SaveShoppingItemUseCase(
            shoppingDataSource = shoppingDataSource,
        )

    @Single
    fun provideSaveShoppingListUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): SaveShoppingListUseCase =
        SaveShoppingListUseCase(
            shoppingDataSource = shoppingDataSource,
        )

    @Single
    fun provideGetExpenseAccountsStreamUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
    ): GetExpenseAccountsStreamUseCase =
        GetExpenseAccountsStreamUseCase(expenseAccountDataSource = expenseAccountDataSource)

    @Single
    fun provideGetSelectedExpenseAccountStreamUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
    ): GetSelectedExpenseAccountStreamUseCase =
        GetSelectedExpenseAccountStreamUseCase(expenseAccountDataSource = expenseAccountDataSource)

    @Single
    fun provideSelectExpenseAccountUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
    ): SelectExpenseAccountUseCase =
        SelectExpenseAccountUseCase(expenseAccountDataSource = expenseAccountDataSource)

    @Single
    fun provideSaveExpenseAccountUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
        getMembersUseCase: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): SaveExpenseAccountUseCase =
        SaveExpenseAccountUseCase(
            expenseAccountDataSource = expenseAccountDataSource,
            getMembersUseCase = getMembersUseCase,
            context = context,
        )

    @Single
    fun provideGetExpenseAccountUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
    ): GetExpenseAccountUseCase =
        GetExpenseAccountUseCase(expenseAccountDataSource = expenseAccountDataSource)

    @Single
    fun provideGetExpenseUseCase(expenseDataSource: ExpenseDataSource): GetExpenseUseCase =
        GetExpenseUseCase(expenseDataSource = expenseDataSource)

    @Single
    fun provideDeleteExpenseUseCase(expenseDataSource: ExpenseDataSource): DeleteExpenseUseCase =
        DeleteExpenseUseCase(expenseDataSource = expenseDataSource)

    @Single
    fun provideGetExpenseFormUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
        getExpenseUseCase: GetExpenseUseCase,
        householdDataSource: HouseholdDataSource,
        context: HouseholdContextDomain,
    ): GetExpenseFormUseCase =
        GetExpenseFormUseCase(
            expenseAccountDataSource = expenseAccountDataSource,
            getExpenseUseCase = getExpenseUseCase,
            householdDataSource = householdDataSource,
            context = context,
        )

    @Single
    fun provideGetExpenseAccountFormUseCase(
        getExpenseAccountUseCase: GetExpenseAccountUseCase,
        getMembersUseCase: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): GetExpenseAccountFormUseCase =
        GetExpenseAccountFormUseCase(
            getExpenseAccountUseCase = getExpenseAccountUseCase,
            getMembersUseCase = getMembersUseCase,
            context = context,
        )

    @Single
    fun provideResolveExpenseSplitUseCase(): ResolveExpenseSplitUseCase =
        ResolveExpenseSplitUseCase()

    @Single
    fun provideGetShoppingSelectionStreamUseCase(
        shoppingSelectionDataSource: ShoppingSelectionDataSource,
    ): GetShoppingSelectionStreamUseCase =
        GetShoppingSelectionStreamUseCase(shoppingSelectionDataSource = shoppingSelectionDataSource)

    @Single
    fun provideSelectShoppingListUseCase(
        shoppingSelectionDataSource: ShoppingSelectionDataSource,
    ): SelectShoppingListUseCase =
        SelectShoppingListUseCase(shoppingSelectionDataSource = shoppingSelectionDataSource)

    @Single
    fun provideDeleteShoppingListUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): DeleteShoppingListUseCase =
        DeleteShoppingListUseCase(shoppingDataSource = shoppingDataSource)

    @Single
    fun provideDeleteExpenseAccountUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
    ): DeleteExpenseAccountUseCase =
        DeleteExpenseAccountUseCase(expenseAccountDataSource = expenseAccountDataSource)

    @Single
    fun provideRemoveCompletedShoppingItemsUseCase(
        shoppingDataSource: ShoppingDataSource,
    ): RemoveCompletedShoppingItemsUseCase =
        RemoveCompletedShoppingItemsUseCase(shoppingDataSource = shoppingDataSource)

    @Single
    fun provideRevokeInvitationUseCase(
        householdDataSource: HouseholdDataSource,
    ): RevokeInvitationUseCase = RevokeInvitationUseCase(householdDataSource = householdDataSource)

    @Single
    fun provideGetFeedSubscriptionsStreamUseCase(
        feedSubscriptionDataSource: FeedSubscriptionDataSource,
    ): GetFeedSubscriptionsStreamUseCase =
        GetFeedSubscriptionsStreamUseCase(feedSubscriptionDataSource = feedSubscriptionDataSource)

    @Single
    fun provideRefreshFeedSubscriptionUseCase(
        feedSubscriptionDataSource: FeedSubscriptionDataSource,
    ): RefreshFeedSubscriptionUseCase =
        RefreshFeedSubscriptionUseCase(feedSubscriptionDataSource = feedSubscriptionDataSource)

    @Single
    fun provideUpdateFeedSubscriptionUseCase(
        feedSubscriptionDataSource: FeedSubscriptionDataSource,
    ): UpdateFeedSubscriptionUseCase =
        UpdateFeedSubscriptionUseCase(feedSubscriptionDataSource = feedSubscriptionDataSource)

    @Single
    fun provideRemoveFeedSubscriptionUseCase(
        feedSubscriptionDataSource: FeedSubscriptionDataSource,
    ): RemoveFeedSubscriptionUseCase =
        RemoveFeedSubscriptionUseCase(feedSubscriptionDataSource = feedSubscriptionDataSource)

    @Single
    fun provideRefreshFeedsUseCase(
        feedSubscriptionDataSource: FeedSubscriptionDataSource,
    ): RefreshFeedsUseCase =
        RefreshFeedsUseCase(feedSubscriptionDataSource = feedSubscriptionDataSource)

    @Single
    fun providePrepareReceiptPhotoUseCase(
        expenseReceiptDataSource: ExpenseReceiptDataSource,
    ): PrepareReceiptPhotoUseCase =
        PrepareReceiptPhotoUseCase(expenseReceiptDataSource = expenseReceiptDataSource)

    @Single
    fun provideGetExpenseReceiptUseCase(
        expenseReceiptDataSource: ExpenseReceiptDataSource,
    ): GetExpenseReceiptUseCase =
        GetExpenseReceiptUseCase(expenseReceiptDataSource = expenseReceiptDataSource)

    @Single
    fun provideDownloadReceiptPhotoUseCase(
        expenseReceiptDataSource: ExpenseReceiptDataSource,
    ): DownloadReceiptPhotoUseCase =
        DownloadReceiptPhotoUseCase(expenseReceiptDataSource = expenseReceiptDataSource)

    @Single
    fun provideSaveExpenseReceiptUseCase(
        expenseReceiptDataSource: ExpenseReceiptDataSource,
    ): SaveExpenseReceiptUseCase =
        SaveExpenseReceiptUseCase(expenseReceiptDataSource = expenseReceiptDataSource)

    @Single
    fun provideRemoveExpenseReceiptUseCase(
        expenseReceiptDataSource: ExpenseReceiptDataSource,
    ): RemoveExpenseReceiptUseCase =
        RemoveExpenseReceiptUseCase(expenseReceiptDataSource = expenseReceiptDataSource)

    @Single
    fun provideSaveSettlementUseCase(expenseDataSource: ExpenseDataSource): SaveSettlementUseCase =
        SaveSettlementUseCase(expenseDataSource)

    @Single
    fun provideRemoveSettlementUseCase(
        expenseDataSource: ExpenseDataSource,
    ): RemoveSettlementUseCase = RemoveSettlementUseCase(expenseDataSource)

    @Single
    fun provideGetSettlementUseCase(expenseDataSource: ExpenseDataSource): GetSettlementUseCase =
        GetSettlementUseCase(expenseDataSource = expenseDataSource)

    @Single
    fun provideGetSettlementFormUseCase(
        expenseAccountDataSource: ExpenseAccountDataSource,
        getSettlementUseCase: GetSettlementUseCase,
        householdDataSource: HouseholdDataSource,
        context: HouseholdContextDomain,
    ): GetSettlementFormUseCase =
        GetSettlementFormUseCase(
            expenseAccountDataSource = expenseAccountDataSource,
            getSettlementUseCase = getSettlementUseCase,
            householdDataSource = householdDataSource,
            context = context,
        )
}
