package com.eyther.lumbridge.di.household.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.features.FeatureGate
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonMemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonRecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.MemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedDataSource
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.household.data.network.datasource.CommonFeatureGate
import com.eyther.lumbridge.features.household.data.network.datasource.CommonHouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.data.export.CommonStatementExportDataSource
import com.eyther.lumbridge.features.insights.data.network.datasource.CommonStatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.shopping.data.local.datasource.CommonShoppingSelectionDataSource
import com.eyther.lumbridge.features.shopping.data.network.datasource.CommonShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingSelectionDataSource
import com.eyther.lumbridge.features.tasks.data.network.datasource.CommonTaskDataSource
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class HouseholdDataSourceModule(
    private val context: HouseholdContextDomain,
    private val access: SupabaseAccess,
    private val householdDataSource: HouseholdDataSource,
    private val analyticsDataSource: AnalyticsDataSource,
    private val settingsDataSource: SettingsDataSource,
) {

    @Single
    fun provideHouseholdManagementDataSource(): HouseholdManagementDataSource =
        CommonHouseholdManagementDataSource(access = access, context = context)

    @Single
    fun provideMemberAccountPositionDataSource(): MemberAccountPositionDataSource =
        CommonMemberAccountPositionDataSource(access = access, context = context)

    @Single fun provideHouseholdContextDomain(): HouseholdContextDomain = context

    @Single fun provideSupabaseAccess(): SupabaseAccess = access

    @Single fun provideHouseholdDataSource(): HouseholdDataSource = householdDataSource

    @Single fun provideAnalyticsDataSource(): AnalyticsDataSource = analyticsDataSource

    @Single
    fun provideShoppingSelectionDataSource(
        context: HouseholdContextDomain,
    ): ShoppingSelectionDataSource = CommonShoppingSelectionDataSource(context = context)

    @Single
    fun provideShoppingDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): ShoppingDataSource =
        CommonShoppingDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideExpenseReceiptDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): ExpenseReceiptDataSource = CommonExpenseReceiptDataSource(access = access, context = context)

    @Single
    fun provideTaskDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): TaskDataSource =
        CommonTaskDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideFeatureGate(access: SupabaseAccess, context: HouseholdContextDomain): FeatureGate =
        CommonFeatureGate(access = access, context = context)

    @Single
    fun provideExpenseAccountDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): ExpenseAccountDataSource = CommonExpenseAccountDataSource(access = access, context = context)

    @Single
    fun provideExpenseDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): ExpenseDataSource =
        CommonExpenseDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideRecurringExpenseDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): RecurringExpenseDataSource =
        CommonRecurringExpenseDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideFeedDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): FeedDataSource =
        CommonFeedDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideFeedSubscriptionDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): FeedSubscriptionDataSource =
        CommonFeedSubscriptionDataSource(access = access, context = context)

    @Single
    fun provideStatementDataSource(
        access: SupabaseAccess,
        context: HouseholdContextDomain,
    ): StatementDataSource =
        CommonStatementDataSource(
            access = access,
            context = context,
        )

    @Single
    fun provideStatementExportDataSource(): StatementExportDataSource =
        CommonStatementExportDataSource()

    @Single fun provideSettingsDataSource(): SettingsDataSource = settingsDataSource
}
