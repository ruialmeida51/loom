package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.bill_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.bill_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringExpenseUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RecurringExpensesState(
    val userId: String = "",
    val deletion: RecurringExpenseDomain? = null,
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

    /** Where "New recurring bill" adds one: the account shown, when this person can add to it. */
    val newBillAccountId: String? = account?.takeIf { it.canAddExpense(userId = userId) }?.id

    val deletionConfirmation: DestructiveConfirmationUi? = deletion?.let {
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.bill_delete_title),
            message =
                TextResource.Resource(
                    Res.string.bill_delete_message,
                    args = listOf(it.title),
                ),
            confirmText = TextResource.Resource(Res.string.detail_delete),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }

    val showEmpty: Boolean = !loading && account != null && rules.isEmpty() && loadError == null
    val currency: String = account?.currency ?: "EUR"
    val displayError: TextResource? = error ?: loadError

    val isOwner: Boolean = role == "owner"
    val rows: ImmutableList<RecurringExpenseUi> =
        rules
            .map { rule ->
                RecurringExpenseUi(
                    rule = rule,
                    userId = userId,
                    role = role,
                    payerName = payerNames[rule.paidBy],
                    currency = currency,
                )
            }
            .toImmutableList()
}
