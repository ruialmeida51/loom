package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.isGuest
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_gets_back
import com.eyther.lumbridge.features.expenses.generated.resources.balance_owes
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemberBalanceRow(member: MemberBalanceUi) {
    val statusColor =
        when {
            member.balance.isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
            member.balance.isOwed -> LoomColors.positive
            else -> MaterialTheme.colorScheme.error
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        modifier = Modifier.padding(vertical = LoomSpacing.small),
    ) {
        MemberAvatar(
            memberId = member.member.id,
            name = member.member.name,
            picturePath = member.member.avatarPath,
        )
        Column(modifier = Modifier.weight(1f)) {
            NameWithGuestTag(
                name = member.member.name.ifBlank { stringResource(Res.string.former_member) },
                isGuest = member.member.isGuest,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (!member.balance.isSettled)
                Text(
                    stringResource(
                        if (member.balance.isOwed) Res.string.balance_gets_back
                        else Res.string.balance_owes,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                )
        }
        Text(
            member.balance.amount,
            style = MaterialTheme.typography.titleSmall,
            color = statusColor,
        )
    }
}
