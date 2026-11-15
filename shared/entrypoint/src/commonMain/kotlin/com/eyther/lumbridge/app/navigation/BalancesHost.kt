package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.BusyNavigationGuard
import com.eyther.lumbridge.app.navigation.components.FlowScaffold
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.model.BalancesDestination
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.navigation.sheet.ExpensesSheetHost
import com.eyther.lumbridge.core.navigation.composables.navigation3.HorizontalSlideNavDisplay
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.rememberNavigationState
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseAccountFormScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseDetailsScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.RecurringExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.presentation.screen.InsightsScreen
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_edit_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_expense_details
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_new_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_insights
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_recurring_expenses
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun BalancesHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onRootChanged: (Boolean) -> Unit,
) {
    val state =
        rememberNavigationState(startRoot = BalancesDestination.Overview) {
            subclass(BalancesDestination.Overview::class)
            subclass(BalancesDestination.Recurring::class)
            subclass(BalancesDestination.Insights::class)
            subclass(BalancesDestination.Expense::class)
            subclass(BalancesDestination.Form::class)
            subclass(BalancesDestination.AccountForm::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val destination = state.getActiveBackStack()?.lastOrNull()
    ScreenAnalyticsEffect(
        screen =
            when (destination) {
                BalancesDestination.Overview -> AnalyticsScreenDomain.EXPENSES
                BalancesDestination.Recurring -> AnalyticsScreenDomain.RECURRING_EXPENSES
                BalancesDestination.Insights -> AnalyticsScreenDomain.INSIGHTS
                is BalancesDestination.Expense -> AnalyticsScreenDomain.EXPENSE_DETAILS
                is BalancesDestination.AccountForm -> AnalyticsScreenDomain.EXPENSE_ACCOUNT_FORM
                else -> null
            },
    )
    LaunchedEffect(destination) { onRootChanged(destination == BalancesDestination.Overview) }
    val overviewViewModel: ExpensesScreenViewModelContract = koinViewModel()
    val pendingPush by PendingPush.destination.collectAsState()
    LaunchedEffect(pendingPush) {
        val push = pendingPush
        if (
            push != null &&
                push.householdId == context.id &&
                (push.route == "money" || push.route == "insights")
        ) {
            overviewViewModel.onAccountSelected(push.accountId ?: context.id)
            PendingPush.clear()
            navigator.resetTo(BalancesDestination.Overview)
            if (push.route == "insights") navigator.navigateTo(BalancesDestination.Insights)
        }
    }
    HorizontalSlideNavDisplay(navigator = navigator, navigationState = state) { key ->
        when (key) {
            BalancesDestination.Overview -> {
                Scaffold(topBar = portalToolbar) { padding ->
                    Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
                        ExpensesSheetHost(
                            viewModel = overviewViewModel,
                            onCreateAccount = {
                                navigator.navigateTo(BalancesDestination.AccountForm())
                            },
                        ) {
                            ExpensesScreen(
                                viewModel = overviewViewModel,
                                onAddExpense = {
                                    overviewViewModel.state.value.account?.let {
                                        navigator.navigateTo(
                                            BalancesDestination.Form(accountId = it.id),
                                        )
                                    }
                                },
                                onOpenExpense = { expense ->
                                    navigator.navigateTo(
                                        BalancesDestination.Expense(
                                            accountId = expense.accountId,
                                            expenseId = expense.id,
                                        ),
                                    )
                                },
                                onCreateAccount = {
                                    navigator.navigateTo(BalancesDestination.AccountForm())
                                },
                                onEditAccount = { account ->
                                    navigator.navigateTo(
                                        BalancesDestination.AccountForm(accountId = account.id),
                                    )
                                },
                                onInsights = { navigator.navigateTo(BalancesDestination.Insights) },
                                onRecurringExpenses = {
                                    navigator.navigateTo(BalancesDestination.Recurring)
                                },
                            )
                        }
                    }
                }
            }
            is BalancesDestination.Form ->
                ExpenseFormHost(
                    accountId = key.accountId,
                    expenseId = key.expenseId,
                    onBack = navigator::goBack,
                    onSaved = {
                        overviewViewModel.onRefresh()
                        navigator.goBack()
                    },
                )
            is BalancesDestination.Expense -> {
                val viewModel: ExpenseDetailsScreenViewModelContract = koinViewModel {
                    parametersOf(
                        ExpenseDetailsScreenViewModelContract.Params(
                            accountId = key.accountId,
                            expenseId = key.expenseId,
                        ),
                    )
                }
                val detailsState by viewModel.state.platformCollectAsStateOnStart()
                FlowScaffold(
                    title = stringResource(Res.string.flow_expense_details),
                    onBack = navigator::goBack,
                    backEnabled = !detailsState.busy,
                ) {
                    ExpenseDetailsScreen(
                        viewModel = viewModel,
                        onEdit = {
                            navigator.navigateTo(
                                BalancesDestination.Form(
                                    accountId = key.accountId,
                                    expenseId = key.expenseId,
                                ),
                            )
                        },
                        onDeleted = {
                            overviewViewModel.onRefresh()
                            navigator.goBack()
                        },
                    )
                }
                BusyNavigationGuard(busy = detailsState.busy)
            }
            is BalancesDestination.AccountForm -> {
                val viewModel: ExpenseAccountFormScreenViewModelContract = koinViewModel {
                    parametersOf(
                        ExpenseAccountFormScreenViewModelContract.Params(accountId = key.accountId),
                    )
                }
                val accountState by viewModel.state.platformCollectAsStateOnStart()
                FlowScaffold(
                    title =
                        stringResource(
                            if (key.accountId == null) Res.string.flow_new_account
                            else Res.string.flow_edit_account,
                        ),
                    onBack = navigator::goBack,
                    backEnabled = !accountState.busy,
                ) {
                    ExpenseAccountFormScreen(
                        viewModel = viewModel,
                        onSaved = { id ->
                            overviewViewModel.onAccountSelected(id)
                            overviewViewModel.onRefresh()
                            navigator.goBack()
                        },
                    )
                }
                BusyNavigationGuard(busy = accountState.busy)
            }
            BalancesDestination.Recurring ->
                FlowScaffold(
                    title = stringResource(Res.string.toolbar_recurring_expenses),
                    onBack = navigator::goBack,
                ) {
                    RecurringExpensesScreen(
                        viewModel = koinViewModel<RecurringExpensesScreenViewModelContract>(),
                    )
                }
            BalancesDestination.Insights ->
                FlowScaffold(
                    title = stringResource(Res.string.toolbar_insights),
                    onBack = navigator::goBack,
                ) {
                    InsightsScreen(
                        viewModel = koinViewModel<InsightsScreenViewModelContract>(),
                        context = context,
                    )
                }
        }
    }
}
