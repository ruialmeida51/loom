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
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_payer
import com.eyther.lumbridge.features.expenses.generated.resources.detail_you
import com.eyther.lumbridge.features.expenses.generated.resources.detail_you_payer
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseShareUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseShareRow(share: ExpenseShareUi) {
    Row(
        modifier = Modifier.padding(LoomSpacing.content),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        MemberAvatar(
            memberId = share.id,
            initials = share.initials,
            picturePath = share.avatarPath,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(
                text = share.name ?: stringResource(Res.string.former_member),
                style = MaterialTheme.typography.titleSmall,
            )
            if (share.isCurrentUser || share.isPayer)
                Text(
                    text =
                        stringResource(
                            when {
                                share.isCurrentUser && share.isPayer -> Res.string.detail_you_payer
                                share.isCurrentUser -> Res.string.detail_you
                                else -> Res.string.detail_payer
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (share.isPayer) LoomColors.positive
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
        Text(text = share.amount, style = MaterialTheme.typography.titleSmall)
    }
}
