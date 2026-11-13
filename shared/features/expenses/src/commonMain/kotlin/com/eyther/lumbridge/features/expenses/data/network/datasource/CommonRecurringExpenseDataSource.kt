package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.*
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.*
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
private data class RecurringDto(
    val id: String,
    val title: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("paid_by") val paidBy: String,
    val category: String,
    @SerialName("next_on") val nextOn: String,
    @SerialName("anchor_day") val anchorDay: Int,
    val active: Boolean,
    @SerialName("cancelled_at") val cancelledAt: String?,
    @SerialName("created_by") val createdBy: String,
    val version: Long,
    val shares: Map<String, Long>,
) {
    fun toDomain() =
        RecurringExpenseDomain(
            id,
            title,
            amountMinor,
            paidBy,
            category,
            nextOn,
            anchorDay,
            active,
            cancelledAt != null,
            createdBy,
            version,
            shares,
        )
}

class CommonRecurringExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : RecurringExpenseDataSource {
    override fun observe() =
        access.watch("recurring_expenses", context.id) {
            access.selectHouseholdRows<RecurringDto>("recurring_expenses", context.id).map {
                it.toDomain()
            }
        }

    override suspend fun control(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc(
                "control_recurring_expense",
                buildJsonObject {
                    put("p_household_id", context.id)
                    put("p_id", rule.id)
                    put("p_action", action.value)
                    put("p_version", rule.version)
                },
            )
            .decodeAs<RecurringDto>()
            .toDomain()
    }
}
