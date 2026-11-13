package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toExpenseError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code != "P0001") return toAppError()

    return when (error) {
        "expenses.message_payment_date_future" -> ExpenseError.FuturePaymentDate(cause = this)
        "expenses.message_household_access_denied" -> ExpenseError.AccessDenied(cause = this)
        "expenses.message_a_cancelled_schedule_cannot_be_resumed" -> RecurringExpenseError.Cancelled(cause = this)
        "expenses.message_this_recurring_expense_no_longer_exists" -> RecurringExpenseError.NotFound(cause = this)
        "expenses.message_only_the_payer_creator_or_household_owner_can_manage" ->
            RecurringExpenseError.PermissionDenied(cause = this)
        "expenses.message_choose_a_valid_schedule_action" -> RecurringExpenseError.InvalidAction(cause = this)
        "expenses.message_this_schedule_changed_refresh_and_try_again" ->
            RecurringExpenseError.Changed(cause = this)
        "expenses.message_all_participants_must_be_current_household_members" ->
            RecurringExpenseError.InactiveParticipants(cause = this)
        "Invalid expense amount or shares." -> ExpenseError.InvalidAmount(cause = this)
        "Choose who to split with." -> ExpenseError.NoParticipants(cause = this)
        "Shares must be integer minor units." -> ExpenseError.InvalidShare(cause = this)
        "Shares must equal the expense total." -> ExpenseError.IncorrectSplitTotal(cause = this)
        "All participants must be current household members." -> ExpenseError.InvalidMembers(cause = this)
        "This expense ID has already been used." -> ExpenseError.AlreadyRecorded(cause = this)
        "This repayment ID has already been used." -> ExpenseError.AlreadyRecorded(cause = this)
        "You can only record repayments you send or receive." -> ExpenseError.SettlementNotPermitted(cause = this)
        "Choose two people and a positive amount." -> ExpenseError.InvalidSettlement(cause = this)
        "Choose current household members." -> ExpenseError.InvalidSettlementMembers(cause = this)
        else -> toAppError()
    }
}
