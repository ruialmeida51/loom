package com.eyther.lumbridge.features.shopping.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

@Serializable
private data class ShoppingDto(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("category") val category: String,
    @SerialName("checked") val checked: Boolean,
    @SerialName("created_by") val createdBy: String,
) {
    fun toDomain() = ShoppingItemDomain(id, name, quantity, category, checked)
}

class CommonShoppingDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ShoppingDataSource {
    private val sample =
        MutableStateFlow(
            listOf(
                ShoppingItemDomain(newId(), "Avocados", "3 ripe ones", "Produce", false),
                ShoppingItemDomain(newId(), "Cherry tomatoes", "1 punnet", "Produce", false),
                ShoppingItemDomain(newId(), "Sourdough bread", "1 loaf", "Bakery", false),
                ShoppingItemDomain(newId(), "Oat milk", "2 cartons", "Dairy & eggs", false),
                ShoppingItemDomain(newId(), "Free-range eggs", "6 eggs", "Dairy & eggs", false),
                ShoppingItemDomain(newId(), "Coffee beans", "250 g", "Pantry", true),
            ),
        )

    override fun observe(): Flow<List<ShoppingItemDomain>> =
        if (context.demo) sample
        else
            access.watch("shopping_items", context.id) {
                access.selectHouseholdRows<ShoppingDto>("shopping_items", context.id).map {
                    it.toDomain()
                }
            }

    override suspend fun add(item: ShoppingItemDomain) = safeApiCall {
        if (context.demo) sample.update { it + item }
        else
            access
                .requireClient()
                .from("shopping_items")
                .insert(
                    ShoppingDto(
                        item.id,
                        context.id,
                        item.name,
                        item.quantity,
                        item.category,
                        item.checked,
                        context.userId,
                    ),
                )
        Unit
    }

    override suspend fun toggle(id: String, checked: Boolean) = safeApiCall {
        if (context.demo)
            sample.update { items ->
                items.map { if (it.id == id) it.copy(checked = checked) else it }
            }
        else
            access.requireClient().from("shopping_items").update({ set("checked", checked) }) {
                filter {
                    eq("id", id)
                    eq("household_id", context.id)
                }
            }
        Unit
    }

    override suspend fun remove(id: String) = safeApiCall {
        if (context.demo) sample.update { items -> items.filterNot { it.id == id } }
        else
            access.requireClient().from("shopping_items").delete {
                filter {
                    eq("id", id)
                    eq("household_id", context.id)
                }
            }
        Unit
    }
}
