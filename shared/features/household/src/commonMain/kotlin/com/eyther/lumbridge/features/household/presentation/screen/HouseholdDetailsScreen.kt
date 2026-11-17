package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.InfoRow
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_currency
import com.eyther.lumbridge.features.household.generated.resources.details_invite
import com.eyther.lumbridge.features.household.generated.resources.details_invite_hint
import com.eyther.lumbridge.features.household.generated.resources.details_members
import com.eyther.lumbridge.features.household.generated.resources.details_time_zone
import com.eyther.lumbridge.features.household.presentation.components.HouseholdMemberRow
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

private val ActionProgressSize =
    20.dp // Match a trailing glyph without shifting the invitation row.

@Composable
fun HouseholdDetailsScreen(
    viewModel: HouseholdDetailsScreenViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
    ) {
        InnerHouseholdDetailsScreen(
            name = context.name,
            currency = context.currency,
            timeZone = context.timeZone,
            members = state.memberRows,
            loading = state.loading,
            busy = state.busy,
            isOwner = state.isOwner,
            error = state.displayError,
            onInvite = viewModel::onCreateInvitation,
            onDismiss = viewModel::onDismissError,
        )
    }
    state.revokeConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            error = state.revokeError,
            confirmationTag = "confirm_revoke_invitation",
            onConfirm = viewModel::onRevokeInvitationConfirmed,
            onDismiss = viewModel::onRevokeInvitationCancelled,
        )
    }
}

@Composable
private fun InnerHouseholdDetailsScreen(
    name: String,
    currency: String,
    timeZone: String,
    members: ImmutableList<HouseholdMemberUi>,
    loading: Boolean,
    busy: Boolean,
    isOwner: Boolean,
    error: TextResource?,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("household_details_content"),
        contentPadding = PaddingValues(LoomSpacing.content),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        item(key = "summary") {
            LoomCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
                ) {
                    LoomIcon(
                        glyph = Glyph.HOME,
                        tint = MaterialTheme.colorScheme.primary,
                        decorative = true,
                    )
                    Text(text = name, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        error?.let { message ->
            item(key = "error") { ErrorNotice(message = message, onDismiss = onDismiss) }
        }
        item(key = "context") {
            GroupedSurface {
                InfoRow(label = stringResource(Res.string.details_currency), value = currency)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(label = stringResource(Res.string.details_time_zone), value = timeZone)
            }
        }
        if (loading || members.isNotEmpty())
            item(key = "members_header") {
                SectionTitle(title = stringResource(Res.string.details_members))
            }
        if (loading) item(key = "loading") { LoomCard { LoadingRows() } }
        else if (members.isNotEmpty())
            item(key = "members") {
                GroupedSurface {
                    members.forEachIndexed { index, member ->
                        key(member.id) {
                            if (index > 0)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            HouseholdMemberRow(member = member)
                        }
                    }
                }
            }
        if (!loading && isOwner)
            item(key = "invite") {
                GroupedSurface {
                    NavigationRow(
                        title = stringResource(Res.string.details_invite),
                        description = stringResource(Res.string.details_invite_hint),
                        glyph = Glyph.PLUS,
                        onClick = onInvite,
                        enabled = !busy,
                        modifier = Modifier.testTag("create_invitation"),
                        trailingContent = {
                            if (busy)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ActionProgressSize),
                                    strokeWidth = 2.dp,
                                )
                            else LoomIcon(glyph = Glyph.ARROW, decorative = true)
                        },
                    )
                }
            }
    }
}
