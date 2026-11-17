package com.eyther.lumbridge.di

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.di.account.AccountDataSourceModule
import com.eyther.lumbridge.di.account.AccountModule
import com.eyther.lumbridge.di.account.ProvidedAccountDataSourceModule
import com.eyther.lumbridge.di.account.module
import com.eyther.lumbridge.di.application.ApplicationModule
import com.eyther.lumbridge.di.application.module
import com.eyther.lumbridge.di.household.HouseholdModule
import com.eyther.lumbridge.di.household.datasource.HouseholdDataSourceModule
import com.eyther.lumbridge.di.household.datasource.ProvidedHouseholdDataSourceModule
import com.eyther.lumbridge.di.household.datasource.module
import com.eyther.lumbridge.di.household.module
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
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
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import org.koin.core.module.Module

fun applicationModules(): List<Module> = listOf(ApplicationModule().module())

fun householdModules(
    context: HouseholdContextDomain,
    access: SupabaseAccess,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
    settingsDataSource: SettingsDataSource,
): List<Module> =
    listOf(
        HouseholdModule().module(),
        HouseholdDataSourceModule(
                context = context,
                access = access,
                householdDataSource = householdDataSource,
                analyticsDataSource = analyticsDataSource,
                settingsDataSource = settingsDataSource,
            )
            .module(),
    )

fun providedHouseholdModules(
    context: HouseholdContextDomain,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
    shoppingDataSource: ShoppingDataSource,
    shoppingSelectionDataSource: ShoppingSelectionDataSource,
    taskDataSource: TaskDataSource,
    expenseAccountDataSource: ExpenseAccountDataSource,
    expenseDataSource: ExpenseDataSource,
    recurringExpenseDataSource: RecurringExpenseDataSource,
    feedDataSource: FeedDataSource,
    feedSubscriptionDataSource: FeedSubscriptionDataSource,
    statementDataSource: StatementDataSource,
    statementExportDataSource: StatementExportDataSource,
    settingsDataSource: SettingsDataSource,
): List<Module> =
    listOf(
        HouseholdModule().module(),
        ProvidedHouseholdDataSourceModule(
                context = context,
                householdDataSource = householdDataSource,
                analyticsDataSource = analyticsDataSource,
                shoppingDataSource = shoppingDataSource,
                shoppingSelectionDataSource = shoppingSelectionDataSource,
                taskDataSource = taskDataSource,
                expenseAccountDataSource = expenseAccountDataSource,
                expenseDataSource = expenseDataSource,
                recurringExpenseDataSource = recurringExpenseDataSource,
                feedDataSource = feedDataSource,
                feedSubscriptionDataSource = feedSubscriptionDataSource,
                statementDataSource = statementDataSource,
                statementExportDataSource = statementExportDataSource,
                settingsDataSource = settingsDataSource,
            )
            .module(),
    )

fun accountModules(
    userId: String,
    access: SupabaseAccess,
    profileDataSource: ProfileDataSource,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
): List<Module> =
    listOf(
        AccountModule().module(),
        AccountDataSourceModule(
                userId = userId,
                access = access,
                profileDataSource = profileDataSource,
                householdDataSource = householdDataSource,
                analyticsDataSource = analyticsDataSource,
            )
            .module(),
    )

fun providedAccountModules(
    profileDataSource: ProfileDataSource,
    householdDataSource: HouseholdDataSource,
    analyticsDataSource: AnalyticsDataSource,
    settingsDataSource: SettingsDataSource,
): List<Module> =
    listOf(
        AccountModule().module(),
        ProvidedAccountDataSourceModule(
                profileDataSource = profileDataSource,
                householdDataSource = householdDataSource,
                analyticsDataSource = analyticsDataSource,
                settingsDataSource = settingsDataSource,
            )
            .module(),
    )
