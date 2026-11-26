package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseFormState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val accounts: List<ExpenseAccountDomain> = emptyList(),
    val switchingAccount: Boolean = false,
    val editing: Boolean = false,
    val expense: ExpenseDomain? = null,
    val members: List<MemberDomain> = emptyList(),
    val title: String = "",
    val amount: String = "",
    val occurredOn: String = "",
    val paidBy: String = "",
    val category: String = "Other",
    val recurring: Boolean = false,
    val split: ExpenseSplitSelectionDomain =
        ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.EQUAL, emptySet()),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val receipt: ExpenseReceiptDomain? = null,
    val receiptPhoto: ReceiptPhotoDomain? = null,
    val receiptLoading: Boolean = false,
    val receiptReady: Boolean = false,
    val receiptPreparing: Boolean = false,
    val receiptError: TextResource? = null,
    val receiptPending: Boolean = false,
    val redirectTo: ExpenseFormScreenViewModelContract.RedirectTo? = null,
) {
    val receiptPhotoUi: ReceiptPhotoUi? = receiptPhoto?.let {
        ReceiptPhotoUi(id = it.id, data = it.data)
    }

    val currency: String = account?.currency ?: "EUR"

    /** Each person's share while splitting equally, once an amount is typed. */
    val equalShare: String? =
        if (split.method != ExpenseSplitMethodDomain.EQUAL || split.participants.isEmpty()) null
        else
            runCatching {
                    val total = MoneyDomain.parse(text = amount, currency = currency)
                    ExpenseSplitDomain.equal(total.minorUnits, split.participants.toList())
                        .values
                        .minOrNull()
                        ?.let { MoneyDomain(minorUnits = it, currency = currency).formatted() }
                }
                .getOrNull()

    /** A new expense can move between accounts until it is saved. */
    val canChooseAccount: Boolean =
        !editing && !busy && !receiptPending && !switchingAccount && accounts.size > 1

    /** The day of the month a repeating expense recurs on, from its date. */
    val repeatDay: Int? = occurredOn.split("-").getOrNull(2)?.toIntOrNull()
    val canSave: Boolean =
        !receiptPreparing &&
            !loading &&
            !busy &&
            !switchingAccount &&
            title.isNotBlank() &&
            amount.isNotBlank() &&
            account?.canAddExpense(userId = userId) == true
}
