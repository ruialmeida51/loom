package com.eyther.lumbridge.features.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.ui_a_little_light_a_little_dark_or_follow_your
import com.eyther.lumbridge.features.settings.generated.resources.ui_appearance
import com.eyther.lumbridge.features.settings.generated.resources.ui_gentle_nudges
import com.eyther.lumbridge.features.settings.generated.resources.ui_household_details
import com.eyther.lumbridge.features.settings.generated.resources.ui_invite_someone_home
import com.eyther.lumbridge.features.settings.generated.resources.ui_just_the_way_you_like_it
import com.eyther.lumbridge.features.settings.generated.resources.ui_loom_0_1_0_nlife_woven_together
import com.eyther.lumbridge.features.settings.generated.resources.ui_one_time_invitation_expires_in_48_hours
import com.eyther.lumbridge.features.settings.generated.resources.ui_reminders_and_shared_expenses
import com.eyther.lumbridge.features.settings.generated.resources.ui_sign_out
import com.eyther.lumbridge.features.settings.generated.resources.ui_switch_household
import com.eyther.lumbridge.features.settings.generated.resources.ui_your_little_corner
import com.eyther.lumbridge.features.settings.presentation.mapper.settingsOptionLabel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModelContract,
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerSettingsScreen(
        preferences = state.preferences,
        members = state.members,
        invitation = state.invitation,
        busy = state.busy,
        error = state.error,
        context = context,
        onSwitchHousehold = onSwitchHousehold,
        onTheme = viewModel::onThemeSelected,
        onNotifications = viewModel::onNotificationsChanged,
        onInvite = viewModel::onCreateInvitation,
        onSignOut = onSignOut,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerSettingsScreen(
    preferences: SettingsDomain,
    members: List<MemberDomain>,
    invitation: String?,
    busy: Boolean,
    error: TextResource?,
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
    onTheme: (String) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_just_the_way_you_like_it),
                stringResource(Res.string.ui_your_little_corner),
            )
        }
        item { ErrorNotice(error, onDismiss) }
        item {
            LoomCard {
                Text(
                    stringResource(Res.string.ui_appearance),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(Res.string.ui_a_little_light_a_little_dark_or_follow_your),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { theme ->
                        FilterChip(
                            preferences.theme == theme,
                            { onTheme(theme) },
                            label = { Text(settingsOptionLabel(theme)) },
                        )
                    }
                }
            }
        }
        item {
            LoomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(Res.string.ui_gentle_nudges),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(Res.string.ui_reminders_and_shared_expenses),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(preferences.notifications, onNotifications, enabled = !busy)
                }
            }
        }
        item {
            LoomCard {
                Text(context.name, style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(
                        Res.string.ui_household_details,
                        context.currency,
                        context.timeZone,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                members.forEach { member ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(member.name)
                        Text(
                            settingsOptionLabel(member.role),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (members.any { it.id == context.userId && it.role == "owner" })
                    LoomButton(
                        stringResource(Res.string.ui_invite_someone_home),
                        onInvite,
                        loading = busy,
                    )
                invitation?.let { token ->
                    Text(
                        stringResource(Res.string.ui_one_time_invitation_expires_in_48_hours),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    SelectionContainer { Text(token, style = MaterialTheme.typography.bodySmall) }
                }
                TextButton(onClick = onSwitchHousehold) {
                    Text(stringResource(Res.string.ui_switch_household))
                }
            }
        }

        item {
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.ui_sign_out))
            }
        }
        item {
            Text(
                stringResource(Res.string.ui_loom_0_1_0_nlife_woven_together),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
