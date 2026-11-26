package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_delete
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.mapper.toDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseDetailsState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val expense: ExpenseDomain? = null,
    val members: List<MemberDomain> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val deleteError: TextResource? = null,
    val receipt: ExpenseReceiptDomain? = null,
    val receiptPhoto: ReceiptPhotoDomain? = null,
    val receiptPreview: ReceiptPhotoDomain? = null,
    val receiptPreviewLoading: Boolean = false,
    val receiptPreviewAttempt: Long = 0L,
    val receiptLoading: Boolean = false,
    val receiptReady: Boolean = false,
    val receiptPreparing: Boolean = false,
    val receiptError: TextResource? = null,
    val showReceiptDeletion: Boolean = false,
    val successMessage: TextResource? = null,
    val showDeleteConfirmation: Boolean = false,
    val deleted: Boolean = false,
) {
    val receiptPhotoUi: ReceiptPhotoUi? = receiptPhoto?.let {
        ReceiptPhotoUi(id = it.id, data = it.data)
    }
    val receiptPreviewUi: ReceiptPhotoUi? = receiptPreview?.let {
        ReceiptPhotoUi(id = "${it.id}:$receiptPreviewAttempt", data = it.data)
    }

    val receiptDeleteConfirmation: DestructiveConfirmationUi? =
        if (showReceiptDeletion)
            DestructiveConfirmationUi(
                title = TextResource.Resource(Res.string.receipt_delete_title),
                message = TextResource.Resource(Res.string.receipt_delete_message),
                confirmText = TextResource.Resource(Res.string.receipt_delete),
                cancelText = TextResource.Resource(Res.string.ui_cancel),
            )
        else null

    val deleteConfirmation: DestructiveConfirmationUi? =
        if (showDeleteConfirmation) {
            DestructiveConfirmationUi(
                title = TextResource.Resource(Res.string.detail_delete_title),
                message = TextResource.Resource(Res.string.detail_delete_message),
                confirmText = TextResource.Resource(Res.string.detail_delete),
                cancelText = TextResource.Resource(Res.string.ui_cancel),
            )
        } else null

    private val participants: List<MemberDomain> = account?.withGuests(members) ?: members
    val memberNames: Map<String, String> = participants.associate { it.id to it.name }
    val details: ExpenseDetailsUi? = account?.let {
        expense?.toDetailsUi(
            account = it,
            userId = userId,
            memberNames = memberNames,
            memberPictures = participants.associate { member -> member.id to member.avatarPath },
        )
    }

    val isOwner: Boolean = members.firstOrNull { it.id == userId }?.role == "owner"
    val canEdit: Boolean =
        !busy &&
            account?.canAddExpense(userId = userId) == true &&
            expense?.canManage(userId = userId, isOwner = isOwner) == true
}
