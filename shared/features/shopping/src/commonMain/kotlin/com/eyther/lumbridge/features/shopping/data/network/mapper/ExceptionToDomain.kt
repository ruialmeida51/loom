package com.eyther.lumbridge.features.shopping.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toShoppingError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code == "23505" && error.contains("\"shopping_lists_active_name_idx\"")) {
        return ShoppingError.ListNameAlreadyExists(cause = this)
    }

    if (code != "P0001") return toAppError()

    return when (error) {
        "shopping.message_invalid_completed_selection" ->
            ShoppingError.InvalidCompletedSelection(cause = this)
        "shopping.message_add_between_1_and_50_items_at_once" ->
            ShoppingError.InvalidBatchSize(cause = this)
        "shopping.message_give_the_list_a_name_up_to_60_characters" ->
            ShoppingError.InvalidListName(cause = this)
        "shopping.message_choose_an_active_shopping_list_in_this_household" ->
            ShoppingError.ActiveListRequired(cause = this)
        "shopping.message_household_access_denied" -> ShoppingError.AccessDenied(cause = this)
        "shopping.message_this_list_changed_refresh_and_try_again" ->
            ShoppingError.ListChanged(cause = this)
        "shopping.message_keep_at_least_one_shopping_list_active" ->
            ShoppingError.LastActiveList(cause = this)
        "shopping.message_this_shopping_list_no_longer_exists" ->
            ShoppingError.ListNotFound(cause = this)
        "shopping.message_check_the_item_name_quantity_and_category" ->
            ShoppingError.InvalidItem(cause = this)
        "shopping.message_this_item_changed_refresh_before_undoing_or_editing_it" ->
            ShoppingError.ItemChanged(cause = this)
        "shopping.message_this_shopping_item_no_longer_exists" ->
            ShoppingError.ItemNotFound(cause = this)
        else -> toAppError()
    }
}
