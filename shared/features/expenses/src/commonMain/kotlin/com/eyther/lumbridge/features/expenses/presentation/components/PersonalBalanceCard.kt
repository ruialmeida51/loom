package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_all_settled
import com.eyther.lumbridge.features.expenses.generated.resources.balance_you_settled
import com.eyther.lumbridge.features.expenses.generated.resources.ui_you_owe
import com.eyther.lumbridge.features.expenses.generated.resources.ui_you_re_owed
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PersonalBalanceCard(balance: BalanceSummaryUi, everyoneSettled: Boolean) {
    val contentColor =
        when {
            balance.isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
            balance.isOwed -> LoomColors.positive
            else -> MaterialTheme.colorScheme.error
        }
    LoomCard {
        Surface(
            shape = MaterialTheme.shapes.small,
            color =
                when {
                    balance.isSettled -> MaterialTheme.colorScheme.surfaceContainerHigh
                    balance.isOwed -> LoomColors.positiveContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                },
        ) {
            Text(
                text =
                    stringResource(
                        when {
                            balance.isSettled && everyoneSettled -> Res.string.balance_all_settled
                            balance.isSettled -> Res.string.balance_you_settled
                            balance.isOwed -> Res.string.ui_you_re_owed
                            else -> Res.string.ui_you_owe
                        },
                    ),
                style = MaterialTheme.typography.labelMedium,
                color =
                    when {
                        balance.isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
                        balance.isOwed -> LoomColors.onPositiveContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
                modifier =
                    Modifier.padding(
                        horizontal = LoomSpacing.small,
                        vertical = LoomSpacing.extraSmall,
                    ),
            )
        }
        if (!balance.isSettled)
            Text(
                balance.amount,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
            )
    }
}
