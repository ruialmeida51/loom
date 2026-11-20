package com.eyther.lumbridge.features.household.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.network.mapper.toDomain
import com.eyther.lumbridge.features.household.data.network.mapper.toHouseholdError
import com.eyther.lumbridge.features.household.data.network.model.request.ChangeHouseholdMemberRequest
import com.eyther.lumbridge.features.household.data.network.model.request.DeleteHouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.HouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.ManageHouseholdInvitationRequest
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdInvitationResponse
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdManagementResponse
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonHouseholdManagementDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : HouseholdManagementDataSource {
    override fun stream(): Flow<AppResult<HouseholdManagementDomain>> =
        access
            .watch {
                access
                    .requireClient()
                    .postgrest
                    .rpc("get_household_management", HouseholdRequest(householdId = context.id))
                    .decodeAs<HouseholdManagementResponse>()
                    .toDomain()
            }
            .asApiResult(mapError = Exception::toHouseholdError, retryTransientFailures = true)

    override suspend fun replaceInvitation(
        previous: HouseholdInvitationDomain?,
        requestId: String,
    ): AppResult<HouseholdInvitationDomain> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "manage_household_invitation",
                        ManageHouseholdInvitationRequest(
                            householdId = context.id,
                            action = "replace",
                            invitationId = previous?.id,
                            version = previous?.version,
                            requestId = requestId,
                        ),
                    )
                    .decodeAs<HouseholdInvitationResponse>()
                    .toDomain()
            }
        }

    override suspend fun revokeInvitation(
        invitation: HouseholdInvitationDomain,
        requestId: String,
    ): AppResult<Unit> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "manage_household_invitation",
                        ManageHouseholdInvitationRequest(
                            householdId = context.id,
                            action = "revoke",
                            invitationId = invitation.id,
                            version = invitation.version,
                            requestId = requestId,
                        ),
                    )
            }
            Unit
        }

    override suspend fun changeMember(
        member: MemberDomain,
        action: HouseholdMemberActionDomain,
        requestId: String,
    ): AppResult<Unit> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "change_household_member",
                        ChangeHouseholdMemberRequest(
                            householdId = context.id,
                            memberId = member.id,
                            action = action.name.lowercase(),
                            version = member.version,
                            requestId = requestId,
                        ),
                    )
            }
            Unit
        }

    override suspend fun deleteHousehold(name: String, requestId: String): AppResult<Unit> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "delete_household",
                        DeleteHouseholdRequest(
                            householdId = context.id,
                            name = name,
                            requestId = requestId,
                        ),
                    )
            }
            Unit
        }
}
