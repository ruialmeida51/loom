package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_everyday
import com.eyther.lumbridge.features.expenses.generated.resources.balance_all_settled
import com.eyther.lumbridge.features.expenses.generated.resources.balance_gets_back
import com.eyther.lumbridge.features.expenses.generated.resources.balance_owes
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.departure_balances
import com.eyther.lumbridge.features.expenses.presentation.model.MemberAccountPositionUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.MemberAccountPositionsScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/** Embedded financial feature: membership screens never parse or sum monetary values. */
@Composable
fun MemberAccountPositionsScreen(viewModel: MemberAccountPositionsScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerMemberAccountPositionsScreen(
        positions = state.positions,
        loading = state.loading,
        error = state.error,
        onRetry = viewModel::onRetry,
    )
}

@Composable
private fun InnerMemberAccountPositionsScreen(
    positions: ImmutableList<MemberAccountPositionUi>,
    loading: Boolean,
    error: TextResource?,
    onRetry: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        modifier = Modifier.testTag("departure_balances"),
    ) {
        Text(
            stringResource(Res.string.departure_balances),
            style = MaterialTheme.typography.titleSmall,
        )
        if (loading) LoadingRows(rows = 2)
        else if (error != null) {
            RetryNotice(
                message = error,
                onRetry = onRetry,
                retryText = stringResource(Res.string.balance_retry),
            )
        } else
            GroupedSurface {
                positions.forEachIndexed { index, row ->
                    if (index > 0)
                        LoomDivider()
                    val color =
                        when {
                            row.balance.isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
                            row.balance.isOwed -> LoomColors.positive
                            else -> MaterialTheme.colorScheme.error
                        }
                    Row(Modifier.padding(LoomSpacing.content)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (row.position.isDefault)
                                    stringResource(Res.string.account_everyday)
                                else row.position.accountName,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(
                                    if (row.balance.isSettled) Res.string.balance_all_settled
                                    else if (row.balance.isOwed) Res.string.balance_gets_back
                                    else Res.string.balance_owes,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = color,
                            )
                        }
                        Text(
                            row.balance.amount,
                            style = MaterialTheme.typography.titleSmall,
                            color = color,
                        )
                    }
                }
            }
    }
}
