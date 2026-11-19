package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseFormState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
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
    val canSave: Boolean =
        !receiptPreparing &&
            !loading &&
            !busy &&
            title.isNotBlank() &&
            amount.isNotBlank() &&
            account?.canAddExpense(userId = userId) == true
}
