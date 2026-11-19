package com.eyther.lumbridge.features.shopping.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.data.network.mapper.toAddRequest
import com.eyther.lumbridge.features.shopping.data.network.mapper.toDomain
import com.eyther.lumbridge.features.shopping.data.network.mapper.toGroupRequest
import com.eyther.lumbridge.features.shopping.data.network.mapper.toRequest
import com.eyther.lumbridge.features.shopping.data.network.mapper.toShoppingError
import com.eyther.lumbridge.features.shopping.data.network.model.request.AddShoppingItemsRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.InsertShoppingItemAfterRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.DeleteShoppingListRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.RemoveCompletedShoppingItemsRequest
import com.eyther.lumbridge.features.shopping.data.network.model.request.SaveShoppingGroupRequest
import com.eyther.lumbridge.features.shopping.data.network.model.response.ShoppingItemResponse
import com.eyther.lumbridge.features.shopping.data.network.model.response.ShoppingListResponse
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CommonShoppingDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ShoppingDataSource {

    override fun stream(): Flow<AppResult<ShoppingSnapshotDomain>> {
        return combine(
                access.watch("shopping_lists", context.id) {
                    access
                        .selectHouseholdRows<ShoppingListResponse>("shopping_lists", context.id)
                        .map { it.toDomain() }
                },
                access.watch("shopping_items", context.id) {
                    access
                        .selectHouseholdRows<ShoppingItemResponse>("shopping_items", context.id)
                        .map { it.toDomain() }
                },
            ) { lists, items ->
                ShoppingSnapshotDomain(lists = lists, items = items)
            }
            .asApiResult(mapError = Exception::toShoppingError, retryTransientFailures = true)
    }

    override suspend fun addItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> {
        return safeApiCall(
            mapError = Exception::toShoppingError,
        ) {
            val request =
                AddShoppingItemsRequest(
                    householdId = context.id,
                    items = items.map { it.toAddRequest() },
                )

            access
                .requireClient()
                .postgrest
                .rpc("add_shopping_items", request)
                .decodeList<ShoppingItemResponse>()
                .map { it.toDomain() }
        }
    }

    override suspend fun insertItemAfter(
        item: ShoppingItemDomain,
        afterId: String,
    ): AppResult<List<ShoppingItemDomain>> =
        safeApiCall(mapError = Exception::toShoppingError) {
            val request =
                InsertShoppingItemAfterRequest(
                    householdId = context.id,
                    item = item.toAddRequest(),
                    afterId = afterId,
                )
            access
                .requireClient()
                .postgrest
                .rpc("insert_shopping_item_after", request)
                .decodeList<ShoppingItemResponse>()
                .map { it.toDomain() }
        }

    override suspend fun saveItem(item: ShoppingItemDomain): AppResult<ShoppingItemDomain> {
        return safeApiCall(
            mapError = Exception::toShoppingError,
        ) {
            val request = item.toRequest(householdId = context.id)

            access
                .requireClient()
                .postgrest
                .rpc("save_shopping_item", request)
                .decodeAs<ShoppingItemResponse>()
                .toDomain()
        }
    }

    override suspend fun saveGroup(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> =
        safeApiCall(mapError = Exception::toShoppingError) {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "save_shopping_group",
                    SaveShoppingGroupRequest(
                        householdId = context.id,
                        items = items.map { it.toGroupRequest() },
                    ),
                )
                .decodeList<ShoppingItemResponse>()
                .map { it.toDomain() }
        }

    override suspend fun saveList(list: ShoppingListDomain): AppResult<ShoppingListDomain> {
        return safeApiCall(
            mapError = Exception::toShoppingError,
        ) {
            val request = list.toRequest(householdId = context.id)

            access
                .requireClient()
                .postgrest
                .rpc("save_shopping_list", request)
                .decodeAs<ShoppingListResponse>()
                .toDomain()
        }
    }

    override suspend fun removeCompletedItems(
        items: List<ShoppingItemDomain>,
    ): AppResult<List<ShoppingItemDomain>> =
        safeApiCall(mapError = Exception::toShoppingError) {
            val request =
                RemoveCompletedShoppingItemsRequest(
                    householdId = context.id,
                    listId = items.first().listId,
                    versions = items.associate { it.id to it.version },
                )
            access
                .requireClient()
                .postgrest
                .rpc("remove_completed_shopping_group", request)
                .decodeList<ShoppingItemResponse>()
                .map { it.toDomain() }
        }

    override suspend fun deleteList(list: ShoppingListDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toShoppingError) {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "delete_shopping_list",
                    DeleteShoppingListRequest(
                        householdId = context.id,
                        id = list.id,
                        version = list.version,
                    ),
                )
            Unit
        }
}
