package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.data.demo.DemoHousehold
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
private data class ExpenseDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("paid_by") val paidBy: String,
    @SerialName("category") val category: String,
    @SerialName("occurred_on") val occurredOn: String,
    @SerialName("shares") val shares: Map<String, Long>,
    @SerialName("recurring") val recurring: Boolean,
) {
    fun toDomain() =
        ExpenseDomain(id, title, amountMinor, paidBy, category, occurredOn, shares, recurring)
}

@Serializable
private data class SettlementDto(
    @SerialName("id") val id: String,
    @SerialName("from_user") val from: String,
    @SerialName("to_user") val to: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("occurred_on") val occurredOn: String,
) {
    fun toDomain() = SettlementDomain(id, from, to, amountMinor, occurredOn)
}

class CommonExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseDataSource {
    private val memberIds = listOf(DemoHousehold.USER, DemoHousehold.ALEX, DemoHousehold.SAM)
    private val sample =
        MutableStateFlow(
            listOf(
                ExpenseDomain(
                    newId(),
                    "The weekly shop",
                    8645,
                    DemoHousehold.USER,
                    "Groceries",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(8645, memberIds),
                ),
                ExpenseDomain(
                    newId(),
                    "Internet",
                    3990,
                    DemoHousehold.ALEX,
                    "Bills",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(3990, memberIds),
                    true,
                ),
                ExpenseDomain(
                    newId(),
                    "Friday night dinner",
                    5400,
                    DemoHousehold.SAM,
                    "Food & drink",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(5400, memberIds),
                ),
            ),
        )
    private val sampleSettlements = MutableStateFlow<List<SettlementDomain>>(emptyList())

    override fun observeExpenses(): Flow<List<ExpenseDomain>> =
        if (context.demo) sample
        else
            access.watch("expenses", context.id) {
                access.selectHouseholdRows<ExpenseDto>("expenses_with_shares", context.id).map {
                    it.toDomain()
                }
            }

    override fun observeSettlements(): Flow<List<SettlementDomain>> =
        if (context.demo) sampleSettlements
        else
            access.watch("settlements", context.id) {
                access.selectHouseholdRows<SettlementDto>("settlements", context.id).map {
                    it.toDomain()
                }
            }

    override suspend fun add(expense: ExpenseDomain) = safeApiCall {
        if (context.demo) sample.update { it + expense }
        else
            access
                .requireClient()
                .postgrest
                .rpc(
                    "create_expense",
                    buildJsonObject {
                        put("p_id", expense.id)
                        put("p_household_id", context.id)
                        put("p_title", expense.title)
                        put("p_amount_minor", expense.amountMinor)
                        put("p_paid_by", expense.paidBy)
                        put("p_category", expense.category)
                        put("p_occurred_on", expense.occurredOn)
                        put("p_recurring", expense.recurring)
                        put(
                            "p_shares",
                            buildJsonObject {
                                expense.shares.forEach { (id, amount) -> put(id, amount) }
                            },
                        )
                    },
                )
        Unit
    }

    override suspend fun settle(settlement: SettlementDomain) = safeApiCall {
        if (context.demo) sampleSettlements.update { it + settlement }
        else
            access
                .requireClient()
                .postgrest
                .rpc(
                    "record_settlement",
                    buildJsonObject {
                        put("p_id", settlement.id)
                        put("p_household_id", context.id)
                        put("p_from", settlement.from)
                        put("p_to", settlement.to)
                        put("p_amount_minor", settlement.amountMinor)
                        put("p_occurred_on", settlement.occurredOn)
                    },
                )
        Unit
    }
}
