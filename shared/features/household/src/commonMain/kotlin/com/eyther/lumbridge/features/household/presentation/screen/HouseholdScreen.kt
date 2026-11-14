package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.ui_choose_a_household_create_a_new_one_or_join
import com.eyther.lumbridge.features.household.generated.resources.ui_create_household
import com.eyther.lumbridge.features.household.generated.resources.ui_join_household
import com.eyther.lumbridge.features.household.generated.resources.ui_make_yourself_at_home
import com.eyther.lumbridge.features.household.generated.resources.ui_refresh_households
import com.eyther.lumbridge.features.household.generated.resources.ui_sign_out
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdScreen(
    viewModel: HouseholdScreenViewModelContract,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHouseholdScreen(
        items = state.items,
        busy = state.busy,
        error = state.error,
        onSelect = onSelect,
        onSignOut = onSignOut,
        onDismiss = viewModel::onDismissError,
        onRefresh = viewModel::onRefresh,
        onCreate = onCreate,
        onJoin = onJoin,
    )
}

@Composable
private fun InnerHouseholdScreen(
    items: List<HouseholdDomain>,
    busy: Boolean,
    error: TextResource?,
    onSelect: (HouseholdDomain) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PageHeading(
            stringResource(Res.string.ui_make_yourself_at_home),
            stringResource(Res.string.ui_choose_a_household_create_a_new_one_or_join),
        )
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
        if (items.isNotEmpty()) {
            GroupedSurface {
                items.forEachIndexed { index, household ->
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = household.name,
                        description = household.currency,
                        onClick = { onSelect(household) },
                        enabled = !busy,
                    )
                }
            }
        }
        LoomButton(
            text = stringResource(Res.string.ui_create_household),
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth().testTag("open_create_household"),
            enabled = !busy,
        )
        OutlinedButton(
            onClick = onJoin,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().testTag("open_join_household"),
        ) {
            Text(stringResource(Res.string.ui_join_household))
        }
        TextButton(onClick = onRefresh, enabled = !busy) {
            Text(stringResource(Res.string.ui_refresh_households))
        }
        TextButton(onClick = onSignOut) { Text(stringResource(Res.string.ui_sign_out)) }
    }
}
