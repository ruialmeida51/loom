package com.eyther.lumbridge.features.shopping.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
private data class ShoppingDto(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
    @SerialName("list_id") val listId: String,
    val version: Long,
    val removed: Boolean,
    val favorite: Boolean,
) {
    fun toDomain() =
        ShoppingItemDomain(
            id,
            name,
            quantity,
            category,
            checked,
            listId,
            version,
            removed,
            favorite,
        )
}

@Serializable
private data class ShoppingListDto(
    val id: String,
    val name: String,
    val archived: Boolean,
    val version: Long,
) {
    fun toDomain() = ShoppingListDomain(id, name, archived, version)
}

class CommonShoppingDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ShoppingDataSource {
    override fun observe(): Flow<ShoppingSnapshotDomain> =
        combine(
            access.watch("shopping_lists", context.id) {
                access.selectHouseholdRows<ShoppingListDto>("shopping_lists", context.id).map {
                    it.toDomain()
                }
            },
            access.watch("shopping_items", context.id) {
                access.selectHouseholdRows<ShoppingDto>("shopping_items", context.id).map {
                    it.toDomain()
                }
            },
        ) { lists, items ->
            ShoppingSnapshotDomain(lists, items)
        }

    override suspend fun add(items: List<ShoppingItemDomain>) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc(
                "add_shopping_items",
                buildJsonObject {
                    put("p_household_id", context.id)
                    put(
                        "p_items",
                        buildJsonArray {
                            items.forEach { item ->
                                add(
                                    buildJsonObject {
                                        put("id", item.id)
                                        put("list_id", item.listId)
                                        put("name", item.name)
                                        put("quantity", item.quantity)
                                        put("category", item.category)
                                    },
                                )
                            }
                        },
                    )
                },
            )
            .decodeList<ShoppingDto>()
            .map { it.toDomain() }
    }

    override suspend fun save(item: ShoppingItemDomain) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc(
                "save_shopping_item",
                buildJsonObject {
                    put("p_household_id", context.id)
                    put("p_id", item.id)
                    put("p_list_id", item.listId)
                    put("p_name", item.name)
                    put("p_quantity", item.quantity)
                    put("p_category", item.category)
                    put("p_checked", item.checked)
                    put("p_favorite", item.favorite)
                    put("p_removed", item.removed)
                    put("p_version", item.version)
                },
            )
            .decodeAs<ShoppingDto>()
            .toDomain()
    }

    override suspend fun saveList(list: ShoppingListDomain) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc(
                "save_shopping_list",
                buildJsonObject {
                    put("p_household_id", context.id)
                    put("p_id", list.id)
                    put("p_name", list.name)
                    put("p_archived", list.archived)
                    put("p_version", list.version)
                },
            )
            .decodeAs<ShoppingListDto>()
            .toDomain()
    }
        .let { result ->
            if (
                result is AppResult.Failure &&
                    result.message.contains("shopping_lists_active_name_idx")
            )
                AppResult.Failure("shopping.message_list_name_exists")
            else result
        }
}
