package com.eyther.lumbridge.features.shopping.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.completed_invalid_selection
import com.eyther.lumbridge.features.shopping.generated.resources.error_access_denied
import com.eyther.lumbridge.features.shopping.generated.resources.error_active_list_required
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_batch_size
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_item
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_item_name
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_list_name
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_list_or_category
import com.eyther.lumbridge.features.shopping.generated.resources.error_invalid_quantity
import com.eyther.lumbridge.features.shopping.generated.resources.error_item_changed
import com.eyther.lumbridge.features.shopping.generated.resources.error_item_not_found
import com.eyther.lumbridge.features.shopping.generated.resources.error_items_already_saved
import com.eyther.lumbridge.features.shopping.generated.resources.error_last_active_list
import com.eyther.lumbridge.features.shopping.generated.resources.error_list_changed
import com.eyther.lumbridge.features.shopping.generated.resources.error_list_name_already_exists
import com.eyther.lumbridge.features.shopping.generated.resources.error_list_not_found
import com.eyther.lumbridge.features.shopping.generated.resources.error_version_required

fun AppError.toShoppingTextResource(): TextResource =
    when (this) {
        is ShoppingError.ListNameAlreadyExists ->
            TextResource.Resource(Res.string.error_list_name_already_exists)
        is ShoppingError.InvalidCompletedSelection ->
            TextResource.Resource(Res.string.completed_invalid_selection)
        is ShoppingError.InvalidBatchSize ->
            TextResource.Resource(Res.string.error_invalid_batch_size)
        is ShoppingError.ItemsAlreadySaved ->
            TextResource.Resource(Res.string.error_items_already_saved)
        is ShoppingError.VersionRequired -> TextResource.Resource(Res.string.error_version_required)
        is ShoppingError.InvalidItemName ->
            TextResource.Resource(Res.string.error_invalid_item_name)
        is ShoppingError.InvalidQuantity -> TextResource.Resource(Res.string.error_invalid_quantity)
        is ShoppingError.InvalidListOrCategory ->
            TextResource.Resource(Res.string.error_invalid_list_or_category)
        is ShoppingError.InvalidListName ->
            TextResource.Resource(Res.string.error_invalid_list_name)
        is ShoppingError.ActiveListRequired ->
            TextResource.Resource(Res.string.error_active_list_required)
        is ShoppingError.AccessDenied -> TextResource.Resource(Res.string.error_access_denied)
        is ShoppingError.ListChanged -> TextResource.Resource(Res.string.error_list_changed)
        is ShoppingError.LastActiveList -> TextResource.Resource(Res.string.error_last_active_list)
        is ShoppingError.ListNotFound -> TextResource.Resource(Res.string.error_list_not_found)
        is ShoppingError.InvalidItem -> TextResource.Resource(Res.string.error_invalid_item)
        is ShoppingError.ItemChanged -> TextResource.Resource(Res.string.error_item_changed)
        is ShoppingError.ItemNotFound -> TextResource.Resource(Res.string.error_item_not_found)
        else -> toTextResource()
    }
