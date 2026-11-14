package com.eyther.lumbridge.di

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.di.application.ApplicationModule
import com.eyther.lumbridge.di.application.module
import com.eyther.lumbridge.di.household.HouseholdModule
import com.eyther.lumbridge.di.household.datasource.HouseholdDataSourceModule
import com.eyther.lumbridge.di.household.datasource.ProvidedHouseholdDataSourceModule
import com.eyther.lumbridge.di.household.datasource.module
import com.eyther.lumbridge.di.household.module
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import org.koin.core.module.Module

fun applicationModules(): List<Module> = listOf(ApplicationModule().module())

fun householdModules(
    context: HouseholdContextDomain,
    access: SupabaseAccess,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
): List<Module> = listOf(
    HouseholdModule().module(),
    HouseholdDataSourceModule(
        context = context,
        access = access,
        householdDataSource = householdDataSource,
        analyticsDataSource = analyticsDataSource,
    ).module(),
)

fun providedHouseholdModules(
    context: HouseholdContextDomain,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
    shoppingDataSource: ShoppingDataSource,
    reminderDataSource: ReminderDataSource,
    expenseDataSource: ExpenseDataSource,
    recurringExpenseDataSource: RecurringExpenseDataSource,
    feedDataSource: FeedDataSource,
    statementDataSource: StatementDataSource,
    statementExportDataSource: StatementExportDataSource,
    settingsDataSource: SettingsDataSource,
): List<Module> = listOf(
    HouseholdModule().module(),
    ProvidedHouseholdDataSourceModule(
        context = context,
        householdDataSource = householdDataSource,
        analyticsDataSource = analyticsDataSource,
        shoppingDataSource = shoppingDataSource,
        reminderDataSource = reminderDataSource,
        expenseDataSource = expenseDataSource,
        recurringExpenseDataSource = recurringExpenseDataSource,
        feedDataSource = feedDataSource,
        statementDataSource = statementDataSource,
        statementExportDataSource = statementExportDataSource,
        settingsDataSource = settingsDataSource,
    ).module(),
)
