package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import kotlinx.coroutines.flow.*

@Composable
fun HouseholdScreen(
    vm: HouseholdScreenViewModelContract,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerHouseholdScreen(
        items = state.items,
        name = state.name,
        token = state.token,
        currencyValue = state.currency,
        busy = state.busy,
        error = state.error,
        onSelect = onSelect,
        onName = vm::name,
        onToken = vm::token,
        onCurrency = vm::currency,
        onSignOut = onSignOut,
        onDismiss = vm::dismiss,
        onRefresh = vm::refresh,
        onCreate = vm::create,
        onJoin = vm::join,
    )
}

@Composable
private fun InnerHouseholdScreen(
    items: List<HouseholdDomain>,
    name: String,
    token: String,
    currencyValue: String,
    busy: Boolean,
    error: String?,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
    onName: (String) -> Unit,
    onToken: (String) -> Unit,
    onCurrency: (String) -> Unit,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PageHeading(
            "YOUR PEOPLE",
            "Make yourself at home.",
            "Choose a household, create a new one, or join your people.",
        )
        ErrorNotice(error, onDismiss)
        items.forEach { household ->
            LoomCard {
                Text(household.name, style = MaterialTheme.typography.titleLarge)
                LoomButton("Open household", { onSelect(household) })
            }
        }
        LoomCard {
            Text("Start a household", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                name,
                onName,
                label = { Text("Household name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Household currency", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                HouseholdCurrencyDomain.supportedCodes.forEach { currency ->
                    FilterChip(
                        selected = currencyValue == currency,
                        onClick = { onCurrency(currency) },
                        label = { Text(currency) },
                    )
                }
            }
            LoomButton("Create household", onCreate, loading = busy)
        }
        LoomCard {
            Text("Have an invitation?", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                token,
                onToken,
                label = { Text("Invitation code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = onJoin, enabled = !busy) { Text("Join household") }
        }
        TextButton(onClick = onSignOut) { Text("Sign out") }
    }
}
