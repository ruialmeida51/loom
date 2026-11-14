package com.eyther.lumbridge.di.household.usecase

import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.domain.usecase.UpdateMemberNicknameUseCase
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class HouseholdUseCaseModule {
    @Single
    fun provideGetMembersUseCase(householdDataSource: HouseholdDataSource): GetMembersUseCase =
        GetMembersUseCase(householdDataSource = householdDataSource)

    @Single
    fun provideUpdateMemberNicknameUseCase(
        householdDataSource: HouseholdDataSource,
    ): UpdateMemberNicknameUseCase =
        UpdateMemberNicknameUseCase(householdDataSource = householdDataSource)

    @Single
    fun provideAddExpenseUseCase(
        expenseDataSource: ExpenseDataSource,
        members: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): AddExpenseUseCase =
        AddExpenseUseCase(
            expenseDataSource = expenseDataSource,
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
        getExpensesStreamUseCase: GetExpensesStreamUseCase,
        getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
    ): GetExpenseLedgerStreamUseCase =
        GetExpenseLedgerStreamUseCase(
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
    ): GetRecurringExpensesStreamUseCase =
        GetRecurringExpensesStreamUseCase(
            recurringExpenseDataSource = recurringExpenseDataSource,
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
        members: GetMembersUseCase,
        context: HouseholdContextDomain,
    ): RecordSettlementUseCase =
        RecordSettlementUseCase(
            expenseDataSource = expenseDataSource,
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
    fun provideGetHomeOverviewStreamUseCase(
        getShoppingStreamUseCase: GetShoppingStreamUseCase,
        getRemindersStreamUseCase: GetRemindersStreamUseCase,
        getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
        context: HouseholdContextDomain,
    ): GetHomeOverviewStreamUseCase =
        GetHomeOverviewStreamUseCase(
            getShoppingStreamUseCase = getShoppingStreamUseCase,
            getRemindersStreamUseCase = getRemindersStreamUseCase,
            getExpenseLedgerStreamUseCase = getExpenseLedgerStreamUseCase,
            context = context,
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
    fun provideAddReminderUseCase(
        reminderDataSource: ReminderDataSource,
        context: HouseholdContextDomain,
    ): AddReminderUseCase =
        AddReminderUseCase(
            reminderDataSource = reminderDataSource,
            context = context,
        )

    @Single
    fun provideCompleteReminderUseCase(
        reminderDataSource: ReminderDataSource,
    ): CompleteReminderUseCase =
        CompleteReminderUseCase(
            reminderDataSource = reminderDataSource,
        )

    @Single
    fun provideGetRemindersStreamUseCase(
        reminderDataSource: ReminderDataSource,
    ): GetRemindersStreamUseCase =
        GetRemindersStreamUseCase(
            reminderDataSource = reminderDataSource,
        )

    @Single
    fun provideRemoveReminderUseCase(
        reminderDataSource: ReminderDataSource,
    ): RemoveReminderUseCase =
        RemoveReminderUseCase(
            reminderDataSource = reminderDataSource,
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
}
