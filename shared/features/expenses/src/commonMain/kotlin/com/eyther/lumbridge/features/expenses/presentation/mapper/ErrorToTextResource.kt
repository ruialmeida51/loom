package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_archived
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_changed
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_denied
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_members
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_missing
import com.eyther.lumbridge.features.expenses.generated.resources.account_error_name
import com.eyther.lumbridge.features.expenses.generated.resources.error_access_denied
import com.eyther.lumbridge.features.expenses.generated.resources.error_already_recorded
import com.eyther.lumbridge.features.expenses.generated.resources.error_amount_not_positive
import com.eyther.lumbridge.features.expenses.generated.resources.error_amount_too_large
import com.eyther.lumbridge.features.expenses.generated.resources.error_duplicate_participants
import com.eyther.lumbridge.features.expenses.generated.resources.error_future_payment_date
import com.eyther.lumbridge.features.expenses.generated.resources.error_incorrect_split_total
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_amount
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_amount_precision
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_members
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_payment_date
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_percentages
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_settlement
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_settlement_members
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_share
import com.eyther.lumbridge.features.expenses.generated.resources.error_invalid_title
import com.eyther.lumbridge.features.expenses.generated.resources.error_no_participants
import com.eyther.lumbridge.features.expenses.generated.resources.error_settlement_not_permitted
import com.eyther.lumbridge.features.expenses.generated.resources.error_unbalanced_ledger
import com.eyther.lumbridge.features.expenses.generated.resources.error_unsupported_currency
import com.eyther.lumbridge.features.expenses.generated.resources.payment_error_changed
import com.eyther.lumbridge.features.expenses.generated.resources.payment_error_permission
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_error_changed
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_error_invalid
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_error_large
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_error_missing
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_error_unavailable
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_cancelled
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_changed
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_inactive_participants
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_invalid_action
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_invalid_anchor_day
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_not_found
import com.eyther.lumbridge.features.expenses.generated.resources.recurring_error_permission_denied

fun AppError.toExpensesTextResource(): TextResource =
    when (this) {
        is SettlementError.Changed -> TextResource.Resource(Res.string.payment_error_changed)
        is SettlementError.PermissionDenied ->
            TextResource.Resource(Res.string.payment_error_permission)
        is ReceiptError.NotFound -> TextResource.Resource(Res.string.receipt_error_missing)
        is ReceiptError.InvalidPhoto -> TextResource.Resource(Res.string.receipt_error_invalid)
        is ReceiptError.PhotoTooLarge -> TextResource.Resource(Res.string.receipt_error_large)
        is ReceiptError.PhotoUnavailable ->
            TextResource.Resource(Res.string.receipt_error_unavailable)
        is ReceiptError.ReceiptChanged -> TextResource.Resource(Res.string.receipt_error_changed)
        is ExpenseAccountError.InvalidName -> TextResource.Resource(Res.string.account_error_name)
        is ExpenseAccountError.InvalidMembers ->
            TextResource.Resource(Res.string.account_error_members)
        is ExpenseAccountError.Archived -> TextResource.Resource(Res.string.account_error_archived)
        is ExpenseAccountError.PermissionDenied ->
            TextResource.Resource(Res.string.account_error_denied)
        is ExpenseAccountError.Changed -> TextResource.Resource(Res.string.account_error_changed)
        is ExpenseAccountError.NotFound -> TextResource.Resource(Res.string.account_error_missing)

        is ExpenseError.InvalidPaymentDate ->
            TextResource.Resource(Res.string.error_invalid_payment_date)
        is ExpenseError.FuturePaymentDate ->
            TextResource.Resource(Res.string.error_future_payment_date)
        is ExpenseError.InvalidTitle -> TextResource.Resource(Res.string.error_invalid_title)
        is ExpenseError.InvalidMembers -> TextResource.Resource(Res.string.error_invalid_members)
        is ExpenseError.InvalidSettlement ->
            TextResource.Resource(Res.string.error_invalid_settlement)
        is ExpenseError.SettlementNotPermitted ->
            TextResource.Resource(Res.string.error_settlement_not_permitted)
        is ExpenseError.InvalidSettlementMembers ->
            TextResource.Resource(Res.string.error_invalid_settlement_members)
        is ExpenseError.UnsupportedCurrency ->
            TextResource.Resource(Res.string.error_unsupported_currency)
        is ExpenseError.InvalidAmountPrecision ->
            TextResource.Resource(Res.string.error_invalid_amount_precision)
        is ExpenseError.AmountTooLarge -> TextResource.Resource(Res.string.error_amount_too_large)
        is ExpenseError.AmountNotPositive ->
            TextResource.Resource(Res.string.error_amount_not_positive)
        is ExpenseError.UnbalancedLedger ->
            TextResource.Resource(Res.string.error_unbalanced_ledger)
        is ExpenseError.InvalidAmount -> TextResource.Resource(Res.string.error_invalid_amount)
        is ExpenseError.DuplicateParticipants ->
            TextResource.Resource(Res.string.error_duplicate_participants)
        is ExpenseError.InvalidPercentages ->
            TextResource.Resource(Res.string.error_invalid_percentages)
        is ExpenseError.NoParticipants -> TextResource.Resource(Res.string.error_no_participants)
        is ExpenseError.InvalidShare -> TextResource.Resource(Res.string.error_invalid_share)
        is ExpenseError.IncorrectSplitTotal ->
            TextResource.Resource(Res.string.error_incorrect_split_total)
        is ExpenseError.AccessDenied -> TextResource.Resource(Res.string.error_access_denied)
        is ExpenseError.AlreadyRecorded -> TextResource.Resource(Res.string.error_already_recorded)
        is RecurringExpenseError.Cancelled ->
            TextResource.Resource(Res.string.recurring_error_cancelled)
        is RecurringExpenseError.NotFound ->
            TextResource.Resource(Res.string.recurring_error_not_found)
        is RecurringExpenseError.PermissionDenied ->
            TextResource.Resource(Res.string.recurring_error_permission_denied)
        is RecurringExpenseError.InvalidAction ->
            TextResource.Resource(Res.string.recurring_error_invalid_action)
        is RecurringExpenseError.Changed ->
            TextResource.Resource(Res.string.recurring_error_changed)
        is RecurringExpenseError.InactiveParticipants ->
            TextResource.Resource(Res.string.recurring_error_inactive_participants)
        is RecurringExpenseError.InvalidAnchorDay ->
            TextResource.Resource(Res.string.recurring_error_invalid_anchor_day)
        else -> toTextResource()
    }
