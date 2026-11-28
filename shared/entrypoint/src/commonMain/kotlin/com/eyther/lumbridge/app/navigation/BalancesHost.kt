package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.components.FinancialRecordActions
import com.eyther.lumbridge.features.expenses.presentation.components.rememberShareExpense
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseAccountFormScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpenseDetailsScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.ExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.screen.RecurringExpensesScreen
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_edit_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_expense_details
import com.eyther.lumbridge.shared.entrypoint.generated.resources.flow_new_account
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
            subclass(BalancesDestination.RecurringForm::class)
            subclass(BalancesDestination.Insights::class)
            subclass(BalancesDestination.Expense::class)
            subclass(BalancesDestination.Form::class)
            subclass(BalancesDestination.Payment::class)
            subclass(BalancesDestination.PaymentForm::class)
            subclass(BalancesDestination.Receipt::class)
            subclass(BalancesDestination.AccountForm::class)
        }
    val navigator = remember(state) { Navigator(state = state) }
    val destination = state.getActiveBackStack()?.lastOrNull()
    var contentRevision by remember { mutableIntStateOf(0) }
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
                            onEditAccount = { account ->
                                navigator.navigateTo(
                                    BalancesDestination.AccountForm(accountId = account.id),
                                )
                            },
                        ) {
                            ExpensesScreen(
                                viewModel = overviewViewModel,
                                onCreateAccount = {
                                    navigator.navigateTo(BalancesDestination.AccountForm())
                                },
                                onAddExpense = {
                                    overviewViewModel.state.value.account?.let {
                                        navigator.navigateTo(
                                            BalancesDestination.Form(accountId = it.id),
                                        )
                                    }
                                },
                                onEditExpense = { expense ->
                                    navigator.navigateTo(
                                        BalancesDestination.Form(
                                            accountId = expense.accountId,
                                            expenseId = expense.id,
                                        ),
                                    )
                                },
                                onOpenExpense = { expense ->
                                    navigator.navigateTo(
                                        BalancesDestination.Expense(
                                            accountId = expense.accountId,
                                            expenseId = expense.id,
                                        ),
                                    )
                                },
                                onOpenPayment = { payment ->
                                    navigator.navigateTo(
                                        BalancesDestination.Payment(
                                            accountId = payment.accountId,
                                            paymentId = payment.id,
                                        ),
                                    )
                                },
                                onEditPayment = { payment ->
                                    navigator.navigateTo(
                                        BalancesDestination.PaymentForm(
                                            accountId = payment.accountId,
                                            paymentId = payment.id,
                                        ),
                                    )
                                },
                                onRecordPayment = { suggestion ->
                                    overviewViewModel.state.value.account?.let { account ->
                                        navigator.navigateTo(
                                            BalancesDestination.PaymentForm(
                                                accountId = account.id,
                                                suggestedFrom = suggestion.from,
                                                suggestedTo = suggestion.to,
                                                suggestedAmountMinor = suggestion.amountMinor,
                                            ),
                                        )
                                    }
                                },
                                onRecurringExpenses = {
                                    navigator.navigateTo(BalancesDestination.Recurring)
                                },
                            )
                        }
                    }
                }
            }
            is BalancesDestination.Receipt ->
                ReceiptViewerHost(
                    accountId = key.accountId,
                    expenseId = key.expenseId,
                    onBack = navigator::goBack,
                )
            is BalancesDestination.Form ->
                ExpenseFormHost(
                    accountId = key.accountId,
                    expenseId = key.expenseId,
                    recurring = key.recurring,
                    onBack = navigator::goBack,
                    onViewReceipt = {
                        key.expenseId?.let {
                            navigator.navigateTo(
                                BalancesDestination.Receipt(
                                    accountId = key.accountId,
                                    expenseId = it,
                                ),
                            )
                        }
                    },
                    onSaved = {
                        contentRevision++
                        overviewViewModel.onContentChanged()
                        navigator.goBack()
                    },
                )
            is BalancesDestination.PaymentForm ->
                PaymentFormHost(
                    accountId = key.accountId,
                    paymentId = key.paymentId,
                    suggestion =
                        if (
                            key.suggestedFrom != null &&
                                key.suggestedTo != null &&
                                key.suggestedAmountMinor != null
                        )
                            SuggestedSettlementDomain(
                                from = key.suggestedFrom,
                                to = key.suggestedTo,
                                amountMinor = key.suggestedAmountMinor,
                            )
                        else null,
                    onBack = navigator::goBack,
                    onSaved = {
                        contentRevision++
                        overviewViewModel.onContentChanged()
                        navigator.goBack()
                    },
                )
            is BalancesDestination.Payment ->
                PaymentDetailsHost(
                    accountId = key.accountId,
                    paymentId = key.paymentId,
                    contentRevision = contentRevision,
                    active = destination == key,
                    onBack = navigator::goBack,
                    onEdit = {
                        navigator.navigateTo(
                            BalancesDestination.PaymentForm(
                                accountId = key.accountId,
                                paymentId = key.paymentId,
                            ),
                        )
                    },
                    onDeleted = {
                        overviewViewModel.onContentChanged()
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
                LaunchedEffect(destination, contentRevision) {
                    if (destination == key) viewModel.onContentChanged()
                }
                FlowScaffold(
                    title = stringResource(Res.string.flow_expense_details),
                    onBack = navigator::goBack,
                    backEnabled = !detailsState.busy,
                    actions = {
                        FinancialRecordActions(
                            testTag = "expense_actions",
                            canEdit = detailsState.canEdit,
                            busy = detailsState.busy,
                            onShare = rememberShareExpense(details = detailsState.details),
                            onEdit = {
                                navigator.navigateTo(
                                    BalancesDestination.Form(
                                        accountId = key.accountId,
                                        expenseId = key.expenseId,
                                    ),
                                )
                            },
                            onDelete = viewModel::onDeleteRequested,
                        )
                    },
                ) {
                    ExpenseDetailsScreen(
                        viewModel = viewModel,
                        onViewReceipt = {
                            navigator.navigateTo(
                                BalancesDestination.Receipt(
                                    accountId = key.accountId,
                                    expenseId = key.expenseId,
                                ),
                            )
                        },
                        onDeleted = {
                            overviewViewModel.onContentChanged()
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
                            overviewViewModel.onContentChanged()
                            navigator.goBack()
                        },
                    )
                }
                BusyNavigationGuard(busy = accountState.busy)
            }
            BalancesDestination.Recurring -> {
                val viewModel: RecurringExpensesScreenViewModelContract = koinViewModel()
                val recurringState by viewModel.state.platformCollectAsStateOnStart()
                LaunchedEffect(destination, contentRevision) {
                    if (destination == key) viewModel.onContentChanged()
                }
                FlowScaffold(
                    title = stringResource(Res.string.toolbar_recurring_expenses),
                    onBack = navigator::goBack,
                    backEnabled = !recurringState.busy,
                ) {
                    RecurringExpensesScreen(
                        viewModel = viewModel,
                        onEdit = { rule ->
                            navigator.navigateTo(
                                BalancesDestination.RecurringForm(
                                    accountId = rule.accountId,
                                    ruleId = rule.id,
                                ),
                            )
                        },
                        onCreate = { accountId ->
                            navigator.navigateTo(
                                BalancesDestination.Form(accountId = accountId, recurring = true),
                            )
                        },
                    )
                }
                BusyNavigationGuard(busy = recurringState.busy)
            }
            is BalancesDestination.RecurringForm ->
                RecurringExpenseFormHost(
                    accountId = key.accountId,
                    ruleId = key.ruleId,
                    onBack = navigator::goBack,
                    onSaved = {
                        contentRevision++
                        overviewViewModel.onContentChanged()
                        navigator.goBack()
                    },
                )
            // Keep old saved routes/push links readable, but open the current account position.
            BalancesDestination.Insights ->
                LaunchedEffect(Unit) {
                    overviewViewModel.onViewChanged(
                        com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
                            .BALANCES,
                    )
                    navigator.resetTo(BalancesDestination.Overview)
                }
        }
    }
}
