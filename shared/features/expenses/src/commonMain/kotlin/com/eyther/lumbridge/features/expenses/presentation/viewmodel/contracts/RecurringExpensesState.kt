package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel_schedule
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel_this_schedule
import com.eyther.lumbridge.features.expenses.generated.resources.ui_future_entries_will_stop_expenses_already_recorded_stay_in
import com.eyther.lumbridge.features.expenses.generated.resources.ui_keep_schedule
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringExpenseUi

data class RecurringExpensesState(
    val userId: String = "",
    val cancellation: RecurringExpenseDomain? = null,
    val account: ExpenseAccountDomain? = null,
    val rules: List<RecurringExpenseDomain> = emptyList(),
    val payerNames: Map<String, String> = emptyMap(),
    val role: String? = null,
    val busy: Boolean = false,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {

    val cancellationConfirmation: DestructiveConfirmationUi? = cancellation?.let {
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.ui_cancel_this_schedule),
            message =
                TextResource.Resource(
                    Res.string.ui_future_entries_will_stop_expenses_already_recorded_stay_in,
                ),
            confirmText = TextResource.Resource(Res.string.ui_cancel_schedule),
            cancelText = TextResource.Resource(Res.string.ui_keep_schedule),
        )
    }

    val showEmpty: Boolean = !loading && account != null && rules.isEmpty() && loadError == null
    val currency: String = account?.currency ?: "EUR"
    val displayError: TextResource? = error ?: loadError

    val isOwner: Boolean = role == "owner"
    val rows: List<RecurringExpenseUi> = rules.map { rule ->
        RecurringExpenseUi(
            rule = rule,
            userId = userId,
            role = role,
            payerName = payerNames[rule.paidBy],
        )
    }
}
