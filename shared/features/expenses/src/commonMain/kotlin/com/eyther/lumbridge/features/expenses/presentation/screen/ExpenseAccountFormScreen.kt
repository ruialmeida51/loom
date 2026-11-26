package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingForm
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_currency
import com.eyther.lumbridge.features.expenses.generated.resources.account_icon
import com.eyther.lumbridge.features.expenses.generated.resources.account_name
import com.eyther.lumbridge.features.expenses.generated.resources.account_participants
import com.eyther.lumbridge.features.expenses.generated.resources.account_save
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.presentation.components.AccountGuestsSection
import com.eyther.lumbridge.features.expenses.presentation.components.AccountIconPicker
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseAccountFormScreen(
    viewModel: ExpenseAccountFormScreenViewModelContract,
    onSaved: (String) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }
    InnerExpenseAccountFormScreen(
        name = state.name,
        currency = state.currency,
        icon = state.icon,
        onIcon = viewModel::onIconChanged,
        members = state.members,
        selected = state.selected,
        guests = state.guests,
        showsGuests = state.showsGuests,
        canAddGuest = state.canAddGuest,
        guestBusyId = state.guestBusyId,
        userId = state.userId,
        editing = state.account != null,
        loading = state.loading,
        busy = state.busy,
        canSave = state.canSave,
        error = state.error,
        onName = viewModel::onNameChanged,
        onCurrency = viewModel::onCurrencyChanged,
        onParticipant = viewModel::onParticipantToggled,
        onGuestAdded = viewModel::onGuestAdded,
        onGuestRenamed = viewModel::onGuestRenamed,
        onGuestRemoved = viewModel::onGuestRemoved,
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerExpenseAccountFormScreen(
    name: String,
    currency: String,
    icon: ExpenseAccountIconDomain,
    onIcon: (ExpenseAccountIconDomain) -> Unit,
    members: List<MemberDomain>,
    selected: Set<String>,
    guests: List<ExpenseGuestDomain>,
    showsGuests: Boolean,
    canAddGuest: Boolean,
    guestBusyId: String?,
    userId: String,
    editing: Boolean,
    loading: Boolean,
    busy: Boolean,
    canSave: Boolean,
    error: TextResource?,
    onName: (String) -> Unit,
    onCurrency: (String) -> Unit,
    onParticipant: (String) -> Unit,
    onGuestAdded: (String) -> Unit,
    onGuestRenamed: (String, String) -> Unit,
    onGuestRemoved: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading) LoadingForm()
            error?.let {
                ErrorNotice(message = it, onDismiss = onDismiss)
                LoomSecondaryButton(
                    text = stringResource(Res.string.balance_retry),
                    onClick = onRetry,
                    modifier = Modifier,
                )
            }
            if (members.isNotEmpty()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onName,
                    singleLine = true,
                    enabled = !busy,
                    label = { Text(stringResource(Res.string.account_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(Res.string.account_icon),
                    style = MaterialTheme.typography.titleSmall,
                )
                AccountIconPicker(selected = icon, enabled = !busy, onSelected = onIcon)
                Text(
                    stringResource(Res.string.account_currency),
                    style = MaterialTheme.typography.titleSmall,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HouseholdCurrencyDomain.entries.forEach { choice ->
                        FilterChip(
                            selected = currency == choice.name,
                            onClick = { onCurrency(choice.name) },
                            enabled = !editing && !busy,
                            label = { Text(choice.name) },
                        )
                    }
                }
                Text(
                    stringResource(Res.string.account_participants),
                    style = MaterialTheme.typography.titleSmall,
                )
                members.forEach { member ->
                    Row {
                        Checkbox(
                            checked = member.id in selected,
                            onCheckedChange = { onParticipant(member.id) },
                            enabled = !editing && !busy && member.id != userId,
                        )
                        Text(member.name, modifier = Modifier.padding(top = 12.dp))
                    }
                }
                if (showsGuests)
                    AccountGuestsSection(
                        guests = guests,
                        canAdd = canAddGuest,
                        busyId = guestBusyId,
                        enabled = !busy,
                        onAdd = onGuestAdded,
                        onRename = onGuestRenamed,
                        onRemove = onGuestRemoved,
                        modifier = Modifier.padding(top = 8.dp),
                    )
            }
        }
        LoomButton(
            text = stringResource(Res.string.account_save),
            onClick = onSave,
            enabled = canSave,
            loading = busy,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("save_expense_account"),
        )
    }
}
