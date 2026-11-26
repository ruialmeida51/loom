package com.eyther.lumbridge.features.account.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.data.network.mapper.toDomain
import com.eyther.lumbridge.features.account.data.network.model.request.SavePaymentHandlesRequest
import com.eyther.lumbridge.features.account.data.network.model.response.PaymentHandlesResponse
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import io.github.jan.supabase.postgrest.from

class CommonPaymentHandlesDataSource(
    private val access: SupabaseAccess,
    private val userId: String,
) : PaymentHandlesDataSource {
    override suspend fun get(userIds: Collection<String>): AppResult<List<PaymentHandlesDomain>> =
        safeApiCall {
            access
                .requireClient()
                .from("payment_handles")
                .select { filter { isIn("user_id", userIds.toList()) } }
                .decodeList<PaymentHandlesResponse>()
                .map { it.toDomain() }
        }

    override suspend fun save(handles: PaymentHandlesDomain): AppResult<PaymentHandlesDomain> =
        safeApiCall {
            access.write { client ->
                if (handles.isEmpty)
                    client.from("payment_handles").delete { filter { eq("user_id", userId) } }
                else
                    client
                        .from("payment_handles")
                        .upsert(
                            SavePaymentHandlesRequest(
                                userId = userId,
                                mbwayPhone = handles.mbwayPhone,
                                revolutUsername = handles.revolutUsername,
                                paypalUsername = handles.paypalUsername,
                            ),
                        )
            }
            handles.copy(userId = userId)
        }
}
