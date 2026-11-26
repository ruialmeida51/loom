package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.InfoRow
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.components.picture.rememberPicturePicker
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.share.rememberTextSharer
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_currency
import com.eyther.lumbridge.features.household.generated.resources.details_invite
import com.eyther.lumbridge.features.household.generated.resources.details_invite_hint
import com.eyther.lumbridge.features.household.generated.resources.details_members
import com.eyther.lumbridge.features.household.generated.resources.details_time_zone
import com.eyther.lumbridge.features.household.generated.resources.invitation_active
import com.eyther.lumbridge.features.household.generated.resources.invitation_copy
import com.eyther.lumbridge.features.household.generated.resources.invitation_expires_at
import com.eyther.lumbridge.features.household.generated.resources.invitation_expiry_details
import com.eyther.lumbridge.features.household.generated.resources.invitation_legacy
import com.eyther.lumbridge.features.household.generated.resources.invitation_replace
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke
import com.eyther.lumbridge.features.household.generated.resources.invitation_share
import com.eyther.lumbridge.features.household.generated.resources.invitation_share_text
import com.eyther.lumbridge.features.household.generated.resources.management_delete_row
import com.eyther.lumbridge.features.household.generated.resources.management_demote
import com.eyther.lumbridge.features.household.generated.resources.management_last_owner_hint
import com.eyther.lumbridge.features.household.generated.resources.management_leave_row
import com.eyther.lumbridge.features.household.generated.resources.management_promote
import com.eyther.lumbridge.features.household.generated.resources.management_remove
import com.eyther.lumbridge.features.household.presentation.components.HouseholdMemberRow
import com.eyther.lumbridge.features.household.presentation.components.HouseholdPictureHeader
import com.eyther.lumbridge.features.household.presentation.model.HouseholdInvitationUi
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdDetailsScreen(
    viewModel: HouseholdDetailsScreenViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val pickPicture =
        rememberPicturePicker(
            onPicked = viewModel::onPicturePicked,
            onError = viewModel::onPictureError,
        )
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        snackbarHost = { SuccessSnackbar(message = state.successMessage) },
    ) {
        InnerHouseholdDetailsScreen(
            householdId = context.id,
            picturePath = state.picturePath,
            canChangePicture = state.canChangePicture,
            savingPicture = state.savingPicture,
            onChangePicture = pickPicture,
            onRemovePicture = viewModel::onRemovePicture,
            name = context.name,
            currency = context.currency,
            timeZone = context.timeZone,
            members = state.memberRows,
            invitation = state.invitationUi,
            loading = state.loading,
            busy = state.busy,
            isOwner = state.isOwner,
            canLeave = state.canLeave,
            error = state.displayError,
            onInvite = viewModel::onCreateInvitation,
            onRevoke = viewModel::onRevokeInvitation,
            onCopied = viewModel::onCopiedInvitation,
            onShareUnavailable = viewModel::onShareUnavailable,
            onMemberAction = viewModel::onMemberAction,
            onLeave = viewModel::onLeaveRequested,
            onDelete = viewModel::onDeleteRequested,
            onDismiss = viewModel::onDismissError,
        )
    }
}

@Composable
private fun InnerHouseholdDetailsScreen(
    householdId: String,
    picturePath: String?,
    canChangePicture: Boolean,
    savingPicture: Boolean,
    onChangePicture: () -> Unit,
    onRemovePicture: () -> Unit,
    name: String,
    currency: String,
    timeZone: String,
    members: ImmutableList<HouseholdMemberUi>,
    invitation: HouseholdInvitationUi?,
    loading: Boolean,
    busy: Boolean,
    isOwner: Boolean,
    canLeave: Boolean,
    error: TextResource?,
    onInvite: () -> Unit,
    onRevoke: () -> Unit,
    onCopied: () -> Unit,
    onShareUnavailable: () -> Unit,
    onMemberAction: (String, HouseholdMemberActionDomain) -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("household_details_content"),
        contentPadding = rootContentPadding(),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        item(key = "summary") {
            HouseholdPictureHeader(
                householdId = householdId,
                name = name,
                picturePath = picturePath,
                enabled = canChangePicture,
                saving = savingPicture,
                onChange = onChangePicture,
                onRemove = onRemovePicture,
            )
        }
        error?.let { item(key = "error") { ErrorNotice(message = it, onDismiss = onDismiss) } }
        item(key = "context") {
            GroupedSurface {
                InfoRow(label = stringResource(Res.string.details_currency), value = currency)
                LoomDivider()
                InfoRow(label = stringResource(Res.string.details_time_zone), value = timeZone)
            }
        }
        item(key = "members_header") {
            SectionTitle(title = stringResource(Res.string.details_members))
        }
        if (loading) item(key = "loading") { LoadingRows() }
        else
            item(key = "members") {
                GroupedSurface {
                    members.forEachIndexed { index, member ->
                        key(member.id) {
                            if (index > 0)
                                LoomDivider()
                            HouseholdMemberRow(member = member) {
                                if (member.canManage)
                                    ActionMenu(
                                        modifier =
                                            Modifier.testTag("member_actions_${member.name}"),
                                        items =
                                            listOf(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            if (member.isOwner)
                                                                Res.string.management_demote
                                                            else Res.string.management_promote,
                                                        ),
                                                    onClick = {
                                                        onMemberAction(
                                                            member.id,
                                                            if (member.isOwner)
                                                                HouseholdMemberActionDomain.DEMOTE
                                                            else
                                                                HouseholdMemberActionDomain.PROMOTE,
                                                        )
                                                    },
                                                    enabled = !busy,
                                                ),
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            Res.string.management_remove,
                                                        ),
                                                    onClick = {
                                                        onMemberAction(
                                                            member.id,
                                                            HouseholdMemberActionDomain.REMOVE,
                                                        )
                                                    },
                                                    enabled = !busy,
                                                    destructive = true,
                                                ),
                                            ),
                                    )
                            }
                        }
                    }
                }
            }
        if (!loading && isOwner)
            item(key = "invite") {
                if (invitation == null)
                    GroupedSurface {
                        NavigationRow(
                            title = stringResource(Res.string.details_invite),
                            description = stringResource(Res.string.details_invite_hint),
                            glyph = Glyph.PLUS,
                            onClick = onInvite,
                            enabled = !busy,
                            modifier = Modifier.testTag("create_invitation"),
                        )
                    }
                else
                    ActiveInvitationCard(
                        invitation = invitation,
                        busy = busy,
                        onReplace = onInvite,
                        onRevoke = onRevoke,
                        onCopied = onCopied,
                        onShareUnavailable = onShareUnavailable,
                    )
            }
        if (!loading)
            item(key = "household_actions") {
                GroupedSurface {
                    NavigationRow(
                        title = stringResource(Res.string.management_leave_row),
                        description =
                            if (canLeave) null
                            else stringResource(Res.string.management_last_owner_hint),
                        glyph = Glyph.LOG_OUT,
                        emphasisColor = MaterialTheme.colorScheme.error,
                        onClick = onLeave,
                        enabled = !busy,
                        modifier = Modifier.testTag("leave_household"),
                    )
                    if (isOwner) {
                        LoomDivider()
                        NavigationRow(
                            title = stringResource(Res.string.management_delete_row),
                            glyph = Glyph.DELETE,
                            emphasisColor = MaterialTheme.colorScheme.error,
                            onClick = onDelete,
                            enabled = !busy,
                            modifier = Modifier.testTag("delete_household"),
                        )
                    }
                }
            }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun ActiveInvitationCard(
    invitation: HouseholdInvitationUi,
    busy: Boolean,
    onReplace: () -> Unit,
    onRevoke: () -> Unit,
    onCopied: () -> Unit,
    onShareUnavailable: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val share = rememberTextSharer()
    val expiry =
        stringResource(
            Res.string.invitation_expiry_details,
            calendarDateText(invitation.expiry.date),
            invitation.hour,
            invitation.minute,
        )
    val code = invitation.invitation.code
    val shareText = code?.let { stringResource(Res.string.invitation_share_text, it, expiry) }
    LoomCard {
        Row {
            Text(
                stringResource(Res.string.invitation_active),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            ActionMenu(
                modifier = Modifier.testTag("invitation_actions"),
                items =
                    listOf(
                        ActionMenuItem(
                            text = TextResource.Resource(Res.string.invitation_replace),
                            onClick = onReplace,
                            enabled = !busy,
                        ),
                        ActionMenuItem(
                            text = TextResource.Resource(Res.string.invitation_revoke),
                            onClick = onRevoke,
                            enabled = !busy,
                            destructive = true,
                        ),
                    ),
            )
        }
        if (code != null) {
            SelectionContainer {
                Text(
                    code,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("active_invitation_code"),
                )
            }
            Text(
                stringResource(Res.string.invitation_expires_at, expiry),
                style = MaterialTheme.typography.bodySmall,
            )
            LoomSecondaryButton(
                text = stringResource(Res.string.invitation_copy),
                onClick = {
                    clipboard.setText(AnnotatedString(code))
                    onCopied()
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().testTag("copy_invitation"),
            )
            LoomSecondaryButton(
                text = stringResource(Res.string.invitation_share),
                onClick = { if (shareText != null && !share(shareText)) onShareUnavailable() },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().testTag("share_invitation"),
            )
        } else
            Text(
                stringResource(Res.string.invitation_legacy),
                style = MaterialTheme.typography.bodySmall,
            )
    }
}
