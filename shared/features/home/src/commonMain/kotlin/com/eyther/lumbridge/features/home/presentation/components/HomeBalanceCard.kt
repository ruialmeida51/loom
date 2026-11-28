package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.LoomTonalButton
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.extensions.shimmer
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_add_expense
import com.eyther.lumbridge.features.home.generated.resources.home_balance_owed
import com.eyther.lumbridge.features.home.generated.resources.home_balance_owes
import com.eyther.lumbridge.features.home.generated.resources.home_balance_settled
import com.eyther.lumbridge.features.home.generated.resources.home_balance_settled_amount
import com.eyther.lumbridge.features.home.generated.resources.home_balances
import com.eyther.lumbridge.features.home.generated.resources.home_counterparty_owes_you
import com.eyther.lumbridge.features.home.generated.resources.home_counterparty_you_owe
import com.eyther.lumbridge.features.home.generated.resources.home_former_member
import com.eyther.lumbridge.features.home.presentation.model.HomeBalanceStatusUi
import com.eyther.lumbridge.features.home.presentation.model.HomeBalanceUi
import org.jetbrains.compose.resources.stringResource

private val CardPadding = 20.dp
private val MemberAvatarSize = 24.dp

/**
 * Home's hero: the user's position on the everyday account, who they settle with, and the two
 * money actions. A null [balance] shows the loading skeleton in the same footprint.
 */
@Composable
internal fun HomeBalanceCard(
    balance: HomeBalanceUi?,
    onAddExpense: () -> Unit,
    onBalances: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LoomCard(
        modifier = modifier.testTag("home_balance_card"),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(CardPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (balance == null) BalanceSkeleton() else BalanceSummary(balance)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LoomButton(
                text = stringResource(Res.string.home_add_expense),
                onClick = onAddExpense,
                modifier = Modifier.weight(1f).testTag("home_add_expense"),
            )
            LoomTonalButton(
                text = stringResource(Res.string.home_balances),
                onClick = onBalances,
                modifier = Modifier.weight(1f).testTag("home_balances"),
            )
        }
    }
}

@Composable
private fun BalanceSummary(balance: HomeBalanceUi) {
    val amountColor =
        when (balance.status) {
            HomeBalanceStatusUi.OWED -> LoomColors.positive
            HomeBalanceStatusUi.OWES -> MaterialTheme.colorScheme.error
            HomeBalanceStatusUi.SETTLED -> MaterialTheme.colorScheme.onSurface
        }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(
                when (balance.status) {
                    HomeBalanceStatusUi.OWED -> Res.string.home_balance_owed
                    HomeBalanceStatusUi.OWES -> Res.string.home_balance_owes
                    HomeBalanceStatusUi.SETTLED -> Res.string.home_balance_settled
                },
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            if (balance.status == HomeBalanceStatusUi.SETTLED)
                stringResource(Res.string.home_balance_settled_amount)
            else balance.amount,
            style =
                if (balance.status == HomeBalanceStatusUi.SETTLED)
                    MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.displayMedium,
            color = amountColor,
            modifier = Modifier.testTag("home_balance_amount"),
        )
    }
    if (balance.counterparties.isNotEmpty())
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val former = stringResource(Res.string.home_former_member)
            balance.counterparties.forEach { person ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MemberAvatar(
                        memberId = person.memberId,
                        name = person.name,
                        picturePath = person.avatarPath,
                        modifier = Modifier.size(MemberAvatarSize),
                    )
                    Text(
                        stringResource(
                            if (person.owesYou) Res.string.home_counterparty_owes_you
                            else Res.string.home_counterparty_you_owe,
                            person.name ?: former,
                            person.amount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
}

@Composable
private fun BalanceSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(96.dp).height(14.dp).shimmer(true, shape = CircleShape))
        Box(Modifier.width(168.dp).height(40.dp).shimmer(true, shape = MaterialTheme.shapes.small))
        Box(Modifier.width(140.dp).height(14.dp).shimmer(true, shape = CircleShape))
    }
}
