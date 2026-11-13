package com.eyther.lumbridge.features.settings.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import kotlinx.coroutines.flow.*

@Composable
fun SettingsScreen(
    vm: SettingsScreenViewModelContract,
    context: HouseholdContextDomain,
    onSwitchHousehold: () -> Unit,
    onSignOut: () -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerSettingsScreen(
        preferences = state.preferences,
        members = state.members,
        invitation = state.invitation,
        busy = state.busy,
        error = state.error,
        context = context,
        onSwitchHousehold = onSwitchHousehold,
        onTheme = vm::theme,
        onNotifications = vm::notifications,
        onInvite = vm::invite,
        onSignOut = onSignOut,
        onDismiss = vm::dismiss,
    )
}

@Composable
private fun InnerSettingsScreen(
    preferences: SettingsDomain,
    members: List<MemberDomain>,
    invitation: String?,
    busy: Boolean,
    error: String?,
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
        item { PageHeading("JUST THE WAY YOU LIKE IT", "Your little corner.") }
        item { ErrorNotice(error, onDismiss) }
        item {
            LoomCard {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                Text(
                    "A little light, a little dark, or follow your device.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { theme ->
                        FilterChip(
                            preferences.theme == theme,
                            { onTheme(theme) },
                            label = { Text(theme.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
        }
        item {
            LoomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Gentle nudges", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Reminders and shared expenses",
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
                    "${context.currency} · ${context.timeZone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                members.forEach { member ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(member.name)
                        Text(
                            member.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (members.any { it.id == context.userId && it.role == "owner" })
                    LoomButton("Invite someone home", onInvite, loading = busy)
                invitation?.let { token ->
                    Text(
                        "One-time invitation · expires in 48 hours",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    SelectionContainer { Text(token, style = MaterialTheme.typography.bodySmall) }
                }
                TextButton(onClick = onSwitchHousehold) { Text("Switch household") }
            }
        }
        if (context.demo)
            item {
                Text(
                    "You’re exploring a sample household. Changes last for this session.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        item {
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(if (context.demo) "Leave the sample household" else "Sign out")
            }
        }
        item {
            Text(
                "Loom · 0.1.0\nLife, woven together.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
