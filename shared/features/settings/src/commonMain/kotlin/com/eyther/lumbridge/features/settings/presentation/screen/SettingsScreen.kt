package com.eyther.lumbridge.features.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.analytics_description
import com.eyther.lumbridge.features.settings.generated.resources.analytics_title
import com.eyther.lumbridge.features.settings.generated.resources.ui_appearance
import com.eyther.lumbridge.features.settings.generated.resources.ui_gentle_nudges
import com.eyther.lumbridge.features.settings.generated.resources.ui_household_details
import com.eyther.lumbridge.features.settings.generated.resources.ui_invite_someone_home
import com.eyther.lumbridge.features.settings.generated.resources.ui_just_the_way_you_like_it
import com.eyther.lumbridge.features.settings.generated.resources.ui_nickname_description
import com.eyther.lumbridge.features.settings.generated.resources.ui_nickname_title
import com.eyther.lumbridge.features.settings.generated.resources.ui_preferences
import com.eyther.lumbridge.features.settings.generated.resources.ui_reminders_and_shared_expenses
import com.eyther.lumbridge.features.settings.generated.resources.ui_sign_out
import com.eyther.lumbridge.features.settings.generated.resources.ui_switch_household
import com.eyther.lumbridge.features.settings.generated.resources.ui_your_little_corner
import com.eyther.lumbridge.features.settings.generated.resources.ui_your_profile
import com.eyther.lumbridge.features.settings.presentation.components.SettingsToggleRow
import com.eyther.lumbridge.features.settings.presentation.mapper.settingsOptionLabel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModelContract,
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerSettingsScreen(
        preferences = state.preferences,
        analytics = state.analytics,
        members = state.members,
        nickname = state.currentMember?.name.orEmpty(),
        canEditNickname = state.canEditNickname,
        canManageHousehold = state.canManageHousehold,
        busy = state.busy,
        error = state.error,
        context = context,
        onNickname = viewModel::onEditNickname,
        onSwitchHousehold = onSwitchHousehold,
        onTheme = viewModel::onThemeSelected,
        onNotifications = viewModel::onNotificationsChanged,
        onAnalyticsConsent = viewModel::onAnalyticsConsentChanged,
        onInvite = viewModel::onCreateInvitation,
        onSignOut = onSignOut,
        onOpenLegalDocument = onOpenLegalDocument,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerSettingsScreen(
    preferences: SettingsDomain,
    analytics: AnalyticsPreferencesDomain,
    members: List<MemberDomain>,
    nickname: String,
    canEditNickname: Boolean,
    canManageHousehold: Boolean,
    busy: Boolean,
    error: TextResource?,
    context: HouseholdContextDomain,
    onNickname: () -> Unit,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
    onTheme: (String) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onAnalyticsConsent: (Boolean) -> Unit,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("settings_content"),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            PageHeading(
                title = stringResource(Res.string.ui_your_little_corner),
                subtitle = stringResource(Res.string.ui_just_the_way_you_like_it),
            )
        }
        error?.let { message ->
            item(key = "error") { ErrorNotice(message = message, onDismiss = onDismiss) }
        }
        item {
            SectionTitle(title = stringResource(Res.string.ui_your_profile))
            GroupedSurface {
                NavigationRow(
                    title = nickname.ifBlank { stringResource(Res.string.ui_nickname_title) },
                    description = stringResource(Res.string.ui_nickname_description),
                    onClick = onNickname,
                    enabled = canEditNickname,
                    modifier = Modifier.testTag("edit_nickname"),
                )
            }
        }
        item {
            SectionTitle(title = stringResource(Res.string.ui_preferences))
            GroupedSurface {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_appearance),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { theme ->
                            FilterChip(
                                selected = preferences.theme == theme.name,
                                onClick = { onTheme(theme.name) },
                                label = { Text(settingsOptionLabel(theme.name)) },
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsToggleRow(
                    title = stringResource(Res.string.ui_gentle_nudges),
                    description = stringResource(Res.string.ui_reminders_and_shared_expenses),
                    checked = preferences.notifications,
                    onCheckedChange = onNotifications,
                    enabled = !busy,
                )
                if (analytics.available) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsToggleRow(
                        title = stringResource(Res.string.analytics_title),
                        description = stringResource(Res.string.analytics_description),
                        checked = analytics.enabled,
                        onCheckedChange = onAnalyticsConsent,
                        enabled = !busy,
                        modifier = Modifier.testTag("analytics_consent"),
                    )
                }
            }
        }
        item {
            SectionTitle(title = context.name)
            Text(
                text =
                    stringResource(
                        Res.string.ui_household_details,
                        context.currency,
                        context.timeZone,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            GroupedSurface {
                members.forEachIndexed { index, member ->
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = member.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = settingsOptionLabel(member.role),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (canManageHousehold) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.ui_invite_someone_home),
                        onClick = onInvite,
                        enabled = !busy,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationRow(
                    title = stringResource(Res.string.ui_switch_household),
                    onClick = onSwitchHousehold,
                    enabled = !busy,
                )
            }
        }
        item { LegalLinks(onOpenDocument = onOpenLegalDocument, showAccountDeletion = true) }
        item {
            OutlinedButton(
                onClick = onSignOut,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_sign_out))
            }
        }
    }
}
