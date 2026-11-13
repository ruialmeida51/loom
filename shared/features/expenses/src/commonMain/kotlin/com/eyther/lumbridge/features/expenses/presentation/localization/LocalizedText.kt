package com.eyther.lumbridge.features.expenses.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.expenses.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("expenses.message_invalid_payment_date") ->
            stringResource(Res.string.message_invalid_payment_date)
        code.contains("expenses.message_payment_date_future") ->
            stringResource(Res.string.message_payment_date_future)
        code.contains("expenses.message_give_the_expense_a_title_up_to_160_characters") ->
            stringResource(Res.string.message_give_the_expense_a_title_up_to_160_characters)
        code.contains("expenses.message_choose_current_members_of_this_household") ->
            stringResource(Res.string.message_choose_current_members_of_this_household)
        code.contains("expenses.message_a_cancelled_schedule_cannot_be_resumed") ->
            stringResource(Res.string.message_a_cancelled_schedule_cannot_be_resumed)
        code.contains("expenses.message_choose_two_different_people_and_a_positive_amount") ->
            stringResource(Res.string.message_choose_two_different_people_and_a_positive_amount)
        code.contains("expenses.message_you_can_record_repayments_you_send_or_receive") ->
            stringResource(Res.string.message_you_can_record_repayments_you_send_or_receive)
        code.contains("expenses.message_choose_current_household_members") ->
            stringResource(Res.string.message_choose_current_household_members)
        code.contains("expenses.message_choose_a_supported_currency") ->
            stringResource(Res.string.message_choose_a_supported_currency)
        code.contains("expenses.message_enter_an_amount_with_at_most_two_decimal_places") ->
            stringResource(Res.string.message_enter_an_amount_with_at_most_two_decimal_places)
        code.contains("expenses.message_that_amount_is_too_large") ->
            stringResource(Res.string.message_that_amount_is_too_large)
        code.contains("expenses.message_enter_an_amount_greater_than_zero") ->
            stringResource(Res.string.message_enter_an_amount_greater_than_zero)
        code.contains("expenses.message_balances_must_reconcile_before_suggesting_repayments") ->
            stringResource(Res.string.message_balances_must_reconcile_before_suggesting_repayments)
        code.contains("expenses.message_enter_a_valid_amount") ->
            stringResource(Res.string.message_enter_a_valid_amount)
        code.contains("expenses.message_choose_distinct_household_members") ->
            stringResource(Res.string.message_choose_distinct_household_members)
        code.contains("expenses.message_percentages_must_add_up_to_100") ->
            stringResource(Res.string.message_percentages_must_add_up_to_100)
        code.contains("expenses.message_choose_who_to_split_with") ->
            stringResource(Res.string.message_choose_who_to_split_with)
        code.contains("expenses.message_each_share_must_be_between_zero_and_the_total") ->
            stringResource(Res.string.message_each_share_must_be_between_zero_and_the_total)
        code.contains("expenses.message_the_shares_must_add_up_to_the_full_amount") ->
            stringResource(Res.string.message_the_shares_must_add_up_to_the_full_amount)
        code.contains("expenses.message_couldn_t_load_recurring_expenses") ->
            stringResource(Res.string.message_couldn_t_load_recurring_expenses)
        code.contains("expenses.message_household_access_denied") ->
            stringResource(Res.string.message_household_access_denied)
        code.contains("expenses.message_this_recurring_expense_no_longer_exists") ->
            stringResource(Res.string.message_this_recurring_expense_no_longer_exists)
        code.contains("expenses.message_only_the_payer_creator_or_household_owner_can_manage") ->
            stringResource(Res.string.message_only_the_payer_creator_or_household_owner_can_manage)
        code.contains("expenses.message_choose_a_valid_schedule_action") ->
            stringResource(Res.string.message_choose_a_valid_schedule_action)
        code.contains("expenses.message_this_schedule_changed_refresh_and_try_again") ->
            stringResource(Res.string.message_this_schedule_changed_refresh_and_try_again)
        code.contains("expenses.message_all_participants_must_be_current_household_members") ->
            stringResource(Res.string.message_all_participants_must_be_current_household_members)
        else -> genericErrorMessage()
    }

@Composable
internal fun localizedToken(value: String): String =
    when (value) {
        "Groceries" -> stringResource(Res.string.token_groceries)
        "Bills" -> stringResource(Res.string.token_bills)
        "Food & drink" -> stringResource(Res.string.token_food_drink)
        "Home" -> stringResource(Res.string.token_home)
        "Other" -> stringResource(Res.string.token_other)
        "Equal" -> stringResource(Res.string.token_equal)
        "Amounts" -> stringResource(Res.string.token_amounts)
        "Percent" -> stringResource(Res.string.token_percent)
        else -> value
    }

@Composable fun expenseCategoryLabel(value: String): String = localizedToken(value)
