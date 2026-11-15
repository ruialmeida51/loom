package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.ListRow
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_invite
import com.eyther.lumbridge.features.household.generated.resources.details_member
import com.eyther.lumbridge.features.household.generated.resources.details_owner
import com.eyther.lumbridge.features.household.generated.resources.details_summary
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdDetailsScreen(
    viewModel: HouseholdDetailsScreenViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHouseholdDetailsScreen(
        name = context.name,
        currency = context.currency,
        timeZone = context.timeZone,
        members = state.members,
        loading = state.loading,
        busy = state.busy,
        canInvite = state.canInvite,
        error = state.error,
        onInvite = viewModel::onCreateInvitation,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerHouseholdDetailsScreen(
    name: String,
    currency: String,
    timeZone: String,
    members: List<MemberDomain>,
    loading: Boolean,
    busy: Boolean,
    canInvite: Boolean,
    error: TextResource?,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(Res.string.details_summary, currency, timeZone),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        error?.let { message -> item { ErrorNotice(message = message, onDismiss = onDismiss) } }
        if (loading) item { LoadingRows() }
        items(members, key = { it.id }) { member ->
            ListRow {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(member.name, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(
                            if (member.role == "owner") Res.string.details_owner
                            else Res.string.details_member,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (canInvite)
            item {
                LoomButton(
                    text = stringResource(Res.string.details_invite),
                    onClick = onInvite,
                    loading = busy,
                )
            }
    }
}
