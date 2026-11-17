package com.eyther.lumbridge.di.household.datasource

import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ProvidedHouseholdDataSourceModule(
    private val context: HouseholdContextDomain,
    private val householdDataSource: HouseholdDataSource,
    private val analyticsDataSource: AnalyticsDataSource,
    private val shoppingSelectionDataSource: ShoppingSelectionDataSource,
    private val shoppingDataSource: ShoppingDataSource,
    private val reminderDataSource: ReminderDataSource,
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val expenseDataSource: ExpenseDataSource,
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
    private val feedDataSource: FeedDataSource,
    private val feedSubscriptionDataSource: FeedSubscriptionDataSource,
    private val statementDataSource: StatementDataSource,
    private val statementExportDataSource: StatementExportDataSource,
    private val settingsDataSource: SettingsDataSource,
) {

    @Single fun provideHouseholdContextDomain(): HouseholdContextDomain = context

    @Single fun provideHouseholdDataSource(): HouseholdDataSource = householdDataSource

    @Single fun provideAnalyticsDataSource(): AnalyticsDataSource = analyticsDataSource

    @Single
    fun provideShoppingSelectionDataSource(): ShoppingSelectionDataSource =
        shoppingSelectionDataSource

    @Single fun provideShoppingDataSource(): ShoppingDataSource = shoppingDataSource

    @Single fun provideReminderDataSource(): ReminderDataSource = reminderDataSource

    @Single
    fun provideExpenseAccountDataSource(): ExpenseAccountDataSource = expenseAccountDataSource

    @Single fun provideExpenseDataSource(): ExpenseDataSource = expenseDataSource

    @Single
    fun provideRecurringExpenseDataSource(): RecurringExpenseDataSource = recurringExpenseDataSource

    @Single fun provideFeedDataSource(): FeedDataSource = feedDataSource

    @Single
    fun provideFeedSubscriptionDataSource(): FeedSubscriptionDataSource = feedSubscriptionDataSource

    @Single fun provideStatementDataSource(): StatementDataSource = statementDataSource

    @Single
    fun provideStatementExportDataSource(): StatementExportDataSource = statementExportDataSource

    @Single fun provideSettingsDataSource(): SettingsDataSource = settingsDataSource
}
