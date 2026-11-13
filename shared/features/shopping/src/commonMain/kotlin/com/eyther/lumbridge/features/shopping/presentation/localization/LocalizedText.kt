package com.eyther.lumbridge.features.shopping.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.shopping.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("shopping.message_list_name_exists") ->
            stringResource(Res.string.message_list_name_exists)
        code.contains("shopping.message_add_between_1_and_50_items_at_once") ->
            stringResource(Res.string.message_add_between_1_and_50_items_at_once)
        code.contains("shopping.message_add_new_items_to_the_list") ->
            stringResource(Res.string.message_add_new_items_to_the_list)
        code.contains("shopping.message_refresh_the_item_before_editing_it") ->
            stringResource(Res.string.message_refresh_the_item_before_editing_it)
        code.contains("shopping.message_give_the_item_a_name_up_to_120_characters") ->
            stringResource(Res.string.message_give_the_item_a_name_up_to_120_characters)
        code.contains("shopping.message_keep_the_quantity_under_40_characters") ->
            stringResource(Res.string.message_keep_the_quantity_under_40_characters)
        code.contains("shopping.message_choose_a_list_and_category") ->
            stringResource(Res.string.message_choose_a_list_and_category)
        code.contains("shopping.message_give_the_list_a_name_up_to_60_characters") ->
            stringResource(Res.string.message_give_the_list_a_name_up_to_60_characters)
        code.contains("shopping.message_couldn_t_load_your_shopping_lists") ->
            stringResource(Res.string.message_couldn_t_load_your_shopping_lists)
        code.contains("shopping.message_item_updated") ->
            stringResource(Res.string.message_item_updated)
        code.contains("shopping.message_item_removed") ->
            stringResource(Res.string.message_item_removed)
        code.contains("shopping.message_favorite_updated") ->
            stringResource(Res.string.message_favorite_updated)
        code.contains("shopping.message_item_moved") ->
            stringResource(Res.string.message_item_moved)
        code.contains("shopping.message_item_restored") ->
            stringResource(Res.string.message_item_restored)
        code.contains("shopping.message_choose_an_active_shopping_list_in_this_household") ->
            stringResource(Res.string.message_choose_an_active_shopping_list_in_this_household)
        code.contains("shopping.message_household_access_denied") ->
            stringResource(Res.string.message_household_access_denied)
        code.contains("shopping.message_this_list_changed_refresh_and_try_again") ->
            stringResource(Res.string.message_this_list_changed_refresh_and_try_again)
        code.contains("shopping.message_keep_at_least_one_shopping_list_active") ->
            stringResource(Res.string.message_keep_at_least_one_shopping_list_active)
        code.contains("shopping.message_this_shopping_list_no_longer_exists") ->
            stringResource(Res.string.message_this_shopping_list_no_longer_exists)
        code.contains("shopping.message_check_the_item_name_quantity_and_category") ->
            stringResource(Res.string.message_check_the_item_name_quantity_and_category)
        code.contains("shopping.message_this_item_changed_refresh_before_undoing_or_editing_it") ->
            stringResource(
                Res.string.message_this_item_changed_refresh_before_undoing_or_editing_it,
            )
        code.contains("shopping.message_this_shopping_item_no_longer_exists") ->
            stringResource(Res.string.message_this_shopping_item_no_longer_exists)
        else -> genericErrorMessage()
    }

@Composable
internal fun localizedToken(value: String): String =
    when (value) {
        "Produce" -> stringResource(Res.string.token_produce)
        "Bakery" -> stringResource(Res.string.token_bakery)
        "Dairy & eggs" -> stringResource(Res.string.token_dairy_eggs)
        "Pantry" -> stringResource(Res.string.token_pantry)
        "Household" -> stringResource(Res.string.token_household)
        else -> value
    }
