package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.payment_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.payment_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.mapper.toDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.PaymentDetailsUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PaymentDetailsState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val payment: SettlementDomain? = null,
    val members: ImmutableList<MemberDomain> = persistentListOf(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val deletingPayment: SettlementDomain? = null,
    val deleteError: TextResource? = null,
    val deleted: Boolean = false,
) {
    val details: PaymentDetailsUi? = account?.let {
        payment?.toDetailsUi(account = it, members = members, userId = userId)
    }
    val canEdit: Boolean =
        !busy &&
            account?.canAddExpense(userId = userId) == true &&
            payment?.canManage(
                userId = userId,
                isOwner = members.any { it.id == userId && it.role == "owner" },
            ) == true
    val deleteConfirmation: DestructiveConfirmationUi? = deletingPayment?.let {
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.payment_delete_title),
            message =
                TextResource.Resource(
                    Res.string.payment_delete_message,
                    args =
                        listOf(MoneyDomain(it.amountMinor, account?.currency ?: "EUR").formatted()),
                ),
            confirmText = TextResource.Resource(Res.string.detail_delete),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
}
