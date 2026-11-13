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
import com.eyther.lumbridge.features.household.generated.resources.*
import com.eyther.lumbridge.features.household.presentation.components.HouseholdBadge
import com.eyther.lumbridge.features.household.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.stringResource

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
            stringResource(Res.string.ui_your_people),
            stringResource(Res.string.ui_make_yourself_at_home),
            stringResource(Res.string.ui_choose_a_household_create_a_new_one_or_join),
        )
        ErrorNotice(localizedMessage(error), onDismiss)
        items.forEach { household ->
            LoomCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HouseholdBadge(household.id, household.name)
                    Text(household.name, style = MaterialTheme.typography.titleLarge)
                }
                LoomButton(stringResource(Res.string.ui_open_household), { onSelect(household) })
            }
        }
        LoomCard {
            Text(
                stringResource(Res.string.ui_start_a_household),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                name,
                onName,
                label = { Text(stringResource(Res.string.ui_household_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(Res.string.ui_household_currency),
                style = MaterialTheme.typography.labelLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                HouseholdCurrencyDomain.supportedCodes.forEach { currency ->
                    FilterChip(
                        selected = currencyValue == currency,
                        onClick = { onCurrency(currency) },
                        label = { Text(currency) },
                    )
                }
            }
            LoomButton(stringResource(Res.string.ui_create_household), onCreate, loading = busy)
        }
        LoomCard {
            Text(
                stringResource(Res.string.ui_have_an_invitation),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                token,
                onToken,
                label = { Text(stringResource(Res.string.ui_invitation_code)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = onJoin, enabled = !busy) {
                Text(stringResource(Res.string.ui_join_household))
            }
        }
        TextButton(onClick = onSignOut) { Text(stringResource(Res.string.ui_sign_out)) }
    }
}
