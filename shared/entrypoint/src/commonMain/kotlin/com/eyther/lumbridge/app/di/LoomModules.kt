package com.eyther.lumbridge.app.di

import com.eyther.lumbridge.core.data.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.usecase.ObserveSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.StartDemoUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.AuthenticationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.usecase.AddExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveExpensesUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveSettlementsUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.ExpensesScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.ObserveFeedUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.HomeScreenViewModel
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.data.network.datasource.CommonHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.insights.data.network.datasource.CommonStatementDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.presentation.viewmodel.InsightsScreenViewModel
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.reminders.data.network.datasource.CommonReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.ObserveRemindersUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.RemindersScreenViewModel
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.settings.data.network.datasource.CommonSettingsDataSource
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.ObserveSettingsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.SettingsScreenViewModel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.shopping.data.network.datasource.CommonShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.ObserveShoppingUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.RemoveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.ToggleShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.ShoppingScreenViewModel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The same explicit DataSource → UseCase → ViewModel contract bindings as Plannot. */
val applicationModule = module {
    single { SupabaseAccess() }
    single<AuthenticationDataSource> { CommonAuthenticationDataSource(get()) }
    single { ObserveSessionUseCase(get()) }
    single { SignInUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { SignOutUseCase(get()) }
    single { StartDemoUseCase(get()) }
    viewModel<AuthenticationScreenViewModelContract> {
        AuthenticationScreenViewModel(get(), get(), get())
    }
    single<HouseholdDataSource> { CommonHouseholdDataSource(get()) }
    single { GetHouseholdsUseCase(get()) }
    single { CreateHouseholdUseCase(get()) }
    single { JoinHouseholdUseCase(get()) }
    viewModel<HouseholdScreenViewModelContract> { HouseholdScreenViewModel(get(), get(), get()) }
}

/** A fresh graph per household prevents one household's state from leaking into another. */
fun householdModule(
    context: HouseholdContextDomain,
    access: SupabaseAccess,
    householdSource: HouseholdDataSource,
) = module {
    single { context }
    single { access }
    single<HouseholdDataSource> { householdSource }
    single { GetMembersUseCase(get()) }
    single { CreateInvitationUseCase(get()) }
    single<ShoppingDataSource> { CommonShoppingDataSource(get(), get()) }
    single { ObserveShoppingUseCase(get()) }
    single { AddShoppingItemUseCase(get()) }
    single { ToggleShoppingItemUseCase(get()) }
    single { RemoveShoppingItemUseCase(get()) }
    viewModel<ShoppingScreenViewModelContract> {
        ShoppingScreenViewModel(get(), get(), get(), get())
    }
    single<ReminderDataSource> { CommonReminderDataSource(get(), get()) }
    single { ObserveRemindersUseCase(get()) }
    single { AddReminderUseCase(get(), get()) }
    single { CompleteReminderUseCase(get()) }
    single { RemoveReminderUseCase(get()) }
    viewModel<RemindersScreenViewModelContract> {
        RemindersScreenViewModel(get(), get(), get(), get())
    }
    single<ExpenseDataSource> { CommonExpenseDataSource(get(), get()) }
    single { ObserveExpensesUseCase(get()) }
    single { ObserveSettlementsUseCase(get()) }
    single { AddExpenseUseCase(get(), get(), get()) }
    single { RecordSettlementUseCase(get(), get(), get()) }
    viewModel<ExpensesScreenViewModelContract> {
        ExpensesScreenViewModel(get(), get(), get(), get(), get(), get())
    }
    single<FeedDataSource> { CommonFeedDataSource(get(), get()) }
    single { ObserveFeedUseCase(get()) }
    single { FollowFeedUseCase(get()) }
    single { MarkArticleReadUseCase(get()) }
    viewModel<FeedScreenViewModelContract> { FeedScreenViewModel(get(), get(), get()) }
    single<StatementDataSource> { CommonStatementDataSource(get(), get()) }
    single { SaveStatementUseCase(get()) }
    viewModel<InsightsScreenViewModelContract> {
        InsightsScreenViewModel(get(), get(), get(), get())
    }
    single<SettingsDataSource> { CommonSettingsDataSource(get(), get()) }
    single { ObserveSettingsUseCase(get()) }
    single { SetThemeUseCase(get()) }
    single { SetNotificationsUseCase(get()) }
    viewModel<SettingsScreenViewModelContract> {
        SettingsScreenViewModel(get(), get(), get(), get(), get(), get())
    }
    single { GetHomeOverviewUseCase(get(), get(), get(), get(), get()) }
    viewModel<HomeScreenViewModelContract> { HomeScreenViewModel(get()) }
}
