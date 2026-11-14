package com.eyther.lumbridge.app.di

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.createLoomSupabaseClient
import com.eyther.lumbridge.features.authentication.data.local.SecureSessionManager
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.AuthenticationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseDataSource
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonRecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.RecurringExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.data.local.CommonHouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.data.network.datasource.CommonHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.insights.data.export.CommonStatementExportDataSource
import com.eyther.lumbridge.features.insights.data.network.datasource.CommonStatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementExportDataSource
import com.eyther.lumbridge.features.insights.domain.usecase.ExportStatementUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.GetMonthlyStatementStreamUseCase
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.presentation.viewmodel.InsightsScreenViewModel
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.reminders.data.network.datasource.CommonReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.RemindersScreenViewModel
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.settings.data.network.datasource.CommonSettingsDataSource
import com.eyther.lumbridge.features.settings.data.notifications.CommonPushDeviceDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.SettingsScreenViewModel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.data.network.datasource.CommonShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The same explicit DataSource → UseCase → ViewModel contract bindings as Plannot. */
val applicationModule = module {
    single { SupabaseAccess(createLoomSupabaseClient(SecureSessionManager())) }
    single<PushDeviceDataSource> { CommonPushDeviceDataSource(get()) }
    single<AuthenticationDataSource> {
        CommonAuthenticationDataSource(
            access = get(),
            pushDevices = get(),
        )
    }
    single { GetSessionStreamUseCase(get()) }
    single { SignInUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { SignOutUseCase(get()) }
    viewModel<AuthenticationScreenViewModelContract> {
        AuthenticationScreenViewModel(
            signInUseCase = get(),
            registerUseCase = get(),
        )
    }
    single<HouseholdDataSource> { CommonHouseholdDataSource(get()) }
    single<HouseholdSelectionDataSource> { CommonHouseholdSelectionDataSource() }
    single { RememberHouseholdUseCase(get()) }
    single {
        RestoreHouseholdUseCase(
            selectionDataSource = get(),
            getHouseholdsUseCase = get(),
        )
    }

    single { GetHouseholdsUseCase(get()) }
    single { CreateHouseholdUseCase(get()) }
    single { JoinHouseholdUseCase(get()) }
    viewModel<HouseholdScreenViewModelContract> {
        HouseholdScreenViewModel(
            getHouseholdsUseCase = get(),
            createHouseholdUseCase = get(),
            joinHouseholdUseCase = get(),
        )
    }
}

/** A fresh graph per household prevents one household's state from leaking into another. */
fun householdModule(
    context: HouseholdContextDomain,
    access: SupabaseAccess,
    householdSource: HouseholdDataSource,
) = module {
    includes(householdFeaturesModule())
    single { context }
    single { access }
    single<HouseholdDataSource> { householdSource }
    single<ShoppingDataSource> {
        CommonShoppingDataSource(
            access = get(),
            context = get(),
        )
    }
    single<ReminderDataSource> {
        CommonReminderDataSource(
            access = get(),
            context = get(),
        )
    }
    single<ExpenseDataSource> {
        CommonExpenseDataSource(
            access = get(),
            context = get(),
        )
    }
    single<RecurringExpenseDataSource> {
        CommonRecurringExpenseDataSource(
            access = get(),
            context = get(),
        )
    }
    single<FeedDataSource> {
        CommonFeedDataSource(
            access = get(),
            context = get(),
        )
    }
    single<StatementDataSource> {
        CommonStatementDataSource(
            access = get(),
            context = get(),
        )
    }
    single<StatementExportDataSource> { CommonStatementExportDataSource() }
    single<SettingsDataSource> {
        CommonSettingsDataSource(
            access = get(),
            context = get(),
        )
    }
}

/** Domain use cases and screens share the same wiring in the app and preview. */
fun householdFeaturesModule() = module {
    single { GetMembersUseCase(get()) }
    single { CreateInvitationUseCase(get()) }
    single { GetShoppingStreamUseCase(get()) }
    single { AddShoppingItemsUseCase(get()) }
    single { SaveShoppingListUseCase(get()) }
    single { SaveShoppingItemUseCase(get()) }
    viewModel<ShoppingScreenViewModelContract> {
        ShoppingScreenViewModel(
            getShoppingStreamUseCase = get(),
            addShoppingItemsUseCase = get(),
            saveShoppingItemUseCase = get(),
            saveShoppingListUseCase = get(),
        )
    }
    single { GetRemindersStreamUseCase(get()) }
    single {
        AddReminderUseCase(
            reminderDataSource = get(),
            context = get(),
        )
    }
    single { CompleteReminderUseCase(get()) }
    single { RemoveReminderUseCase(get()) }
    viewModel<RemindersScreenViewModelContract> {
        RemindersScreenViewModel(
            getRemindersStreamUseCase = get(),
            addReminderUseCase = get(),
            completeReminderUseCase = get(),
            removeReminderUseCase = get(),
        )
    }
    single { GetRecurringExpensesStreamUseCase(get()) }
    single { ControlRecurringExpenseUseCase(get()) }
    viewModel<RecurringExpensesScreenViewModelContract> {
        RecurringExpensesScreenViewModel(
            getRecurringExpensesStreamUseCase = get(),
            controlRecurringExpenseUseCase = get(),
            getMembersUseCase = get(),
            context = get(),
        )
    }

    single { GetExpensesStreamUseCase(get()) }
    single { GetSettlementsStreamUseCase(get()) }
    single {
        GetExpenseLedgerStreamUseCase(
            getExpensesStreamUseCase = get(),
            getSettlementsStreamUseCase = get(),
        )
    }
    single {
        AddExpenseUseCase(
            expenseDataSource = get(),
            members = get(),
            context = get(),
        )
    }
    single {
        RecordSettlementUseCase(
            expenseDataSource = get(),
            members = get(),
            context = get(),
        )
    }
    viewModel<ExpensesScreenViewModelContract> {
        ExpensesScreenViewModel(
            getExpenseLedgerStreamUseCase = get(),
            getMembersUseCase = get(),
            addExpenseUseCase = get(),
            recordSettlementUseCase = get(),
            context = get(),
        )
    }
    single { GetFeedStreamUseCase(get()) }
    single { FollowFeedUseCase(get()) }
    single { MarkArticleReadUseCase(get()) }
    viewModel<FeedScreenViewModelContract> {
        FeedScreenViewModel(
            getFeedStreamUseCase = get(),
            followFeedUseCase = get(),
            markArticleReadUseCase = get(),
        )
    }
    single { SaveStatementUseCase(get()) }
    single { ExportStatementUseCase(get()) }
    single {
        GetMonthlyStatementStreamUseCase(
            getExpenseLedgerStreamUseCase = get(),
            context = get(),
        )
    }
    viewModel<InsightsScreenViewModelContract> {
        InsightsScreenViewModel(
            getMonthlyStatementStreamUseCase = get(),
            saveStatementUseCase = get(),
            context = get(),
            exportStatementUseCase = get(),
        )
    }
    single { GetSettingsStreamUseCase(get()) }
    single { SetThemeUseCase(get()) }
    single { SetNotificationsUseCase(get()) }
    viewModel<SettingsScreenViewModelContract> {
        SettingsScreenViewModel(
            getSettingsStreamUseCase = get(),
            setThemeUseCase = get(),
            setNotificationsUseCase = get(),
            getMembersUseCase = get(),
            createInvitationUseCase = get(),
            context = get(),
        )
    }
    single {
        GetHomeOverviewStreamUseCase(
            getShoppingStreamUseCase = get(),
            getRemindersStreamUseCase = get(),
            getExpenseLedgerStreamUseCase = get(),
            context = get(),
        )
    }
    viewModel<HomeScreenViewModelContract> { HomeScreenViewModel(get()) }
}
