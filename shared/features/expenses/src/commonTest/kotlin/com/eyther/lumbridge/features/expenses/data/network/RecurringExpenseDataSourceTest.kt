package com.eyther.lumbridge.features.expenses.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonRecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RecurringExpenseDataSourceTest {

    @Test
    fun serverScheduleRefusalsStayTypedThroughTheControlUseCase() = runTest {
        val refusals = listOf(
            "expenses.message_this_schedule_changed_refresh_and_try_again" to RecurringExpenseError.Changed::class,
            "expenses.message_a_cancelled_schedule_cannot_be_resumed" to RecurringExpenseError.Cancelled::class,
            "expenses.message_only_the_payer_creator_or_household_owner_can_manage" to RecurringExpenseError.PermissionDenied::class,
        )

        for ((message, expectedType) in refusals) {
            val client = createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                httpEngine = MockEngine {
                    val body = buildJsonObject {
                        put("code", "P0001")
                        put("message", message)
                    }
                    respond(
                        content = body.toString(),
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }

            try {
                val context = HouseholdContextDomain("home", "alice", "Home", "EUR", "Europe/Lisbon")
                val dataSource = CommonRecurringExpenseDataSource(SupabaseAccess(client), context)
                val rule = RecurringExpenseDomain(
                    id = "rent",
                    title = "Rent",
                    amountMinor = 100_00,
                    paidBy = "alice",
                    category = "Bills",
                    nextOn = LocalDate.parse("2026-12-01"),
                    anchorDay = 1,
                    active = true,
                    cancelled = false,
                    createdBy = "alice",
                    version = 3,
                    shares = mapOf("alice" to 50_00, "bob" to 50_00),
                )
                val result = ControlRecurringExpenseUseCase(dataSource)(rule, RecurringExpenseActionDomain.PAUSE)
                val error = assertIs<AppResult.Failure>(result).error

                kotlin.test.assertEquals(expectedType, error::class)
                assertIs<PostgrestRestException>(error.cause)
            } finally {
                client.close()
            }
        }
    }
}
