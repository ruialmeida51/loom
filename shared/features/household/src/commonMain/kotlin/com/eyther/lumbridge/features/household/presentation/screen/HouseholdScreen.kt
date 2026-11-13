package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.ui_choose_a_household_create_a_new_one_or_join
import com.eyther.lumbridge.features.household.generated.resources.ui_create_household
import com.eyther.lumbridge.features.household.generated.resources.ui_have_an_invitation
import com.eyther.lumbridge.features.household.generated.resources.ui_household_currency
import com.eyther.lumbridge.features.household.generated.resources.ui_household_name
import com.eyther.lumbridge.features.household.generated.resources.ui_invitation_code
import com.eyther.lumbridge.features.household.generated.resources.ui_join_household
import com.eyther.lumbridge.features.household.generated.resources.ui_make_yourself_at_home
import com.eyther.lumbridge.features.household.generated.resources.ui_open_household
import com.eyther.lumbridge.features.household.generated.resources.ui_sign_out
import com.eyther.lumbridge.features.household.generated.resources.ui_start_a_household
import com.eyther.lumbridge.features.household.generated.resources.ui_your_people
import com.eyther.lumbridge.features.household.presentation.components.HouseholdBadge
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdScreen(
    viewModel: HouseholdScreenViewModelContract,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHouseholdScreen(
        items = state.items,
        name = state.name,
        token = state.token,
        currencyValue = state.currency,
        busy = state.busy,
        error = state.error,
        onSelect = onSelect,
        onName = viewModel::onNameChanged,
        onToken = viewModel::onInvitationCodeChanged,
        onCurrency = viewModel::onCurrencyChanged,
        onSignOut = onSignOut,
        onDismiss = viewModel::onDismissError,
        onRefresh = viewModel::onRefresh,
        onCreate = viewModel::onCreateHousehold,
        onJoin = viewModel::onJoinHousehold,
    )
}

@Composable
private fun InnerHouseholdScreen(
    items: List<HouseholdDomain>,
    name: String,
    token: String,
    currencyValue: String,
    busy: Boolean,
    error: TextResource?,
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
        ErrorNotice(error, onDismiss)
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
