package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class RecurringExpenseFormState(
    val userId: String,
    val rule: RecurringExpenseDomain? = null,
    val actorIsOwner: Boolean = false,
    val account: ExpenseAccountDomain? = null,
    val members: ImmutableList<MemberDomain> = persistentListOf(),
    val title: String = "",
    val amount: String = "",
    val paidBy: String = "",
    val nextOn: String = "",
    val split: ExpenseSplitSelectionDomain =
        ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.AMOUNTS, emptySet()),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val saved: Boolean = false,
    val error: TextResource? = null,
) {
    val payerMembers: ImmutableList<MemberDomain> =
        members.filter { it.active || it.id == rule?.paidBy }.toImmutableList()
    val currency: String = account?.currency ?: "EUR"
    val amountMinor: Long? =
        try {
            MoneyDomain.parse(text = amount, currency = currency).minorUnits
        } catch (_: AppError) {
            null
        }
    val canSave: Boolean =
        !loading &&
            !busy &&
            !saved &&
            title.isNotBlank() &&
            amount.isNotBlank() &&
            account?.archived == false &&
            rule?.canManage(
                userId = userId,
                isOwner = actorIsOwner,
            ) == true
}
