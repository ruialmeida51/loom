package com.eyther.lumbridge.features.household.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.ui_choose_a_household_create_a_new_one_or_join
import com.eyther.lumbridge.features.household.generated.resources.ui_create_household
import com.eyther.lumbridge.features.household.generated.resources.ui_join_household
import com.eyther.lumbridge.features.household.generated.resources.ui_make_yourself_at_home
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdScreen(
    viewModel: HouseholdScreenViewModelContract,
    onSelect: (HouseholdDomain) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
    ) {
        InnerHouseholdScreen(
            items = state.items,
            busy = state.loading,
            error = state.error,
            onSelect = onSelect,
            onDismiss = viewModel::onDismissError,
            onCreate = onCreate,
            onJoin = onJoin,
        )
    }
}

@Composable
private fun InnerHouseholdScreen(
    items: List<HouseholdDomain>,
    busy: Boolean,
    error: TextResource?,
    onSelect: (HouseholdDomain) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .testTag("households_content")
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
        if (busy) LoadingRows()
        else if (items.isNotEmpty()) {
            GroupedSurface {
                items.forEachIndexed { index, household ->
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = household.name,
                        glyph = Glyph.HOME,
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
        LoomSecondaryButton(
            text = stringResource(Res.string.ui_join_household),
            onClick = onJoin,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().testTag("open_join_household"),
        )
    }
}
