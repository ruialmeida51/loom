package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.PersonAvatar
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_member
import com.eyther.lumbridge.features.household.generated.resources.details_owner
import com.eyther.lumbridge.features.household.generated.resources.details_you
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HouseholdMemberRow(
    member: HouseholdMemberUi,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier.padding(LoomSpacing.content),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        PersonAvatar(
            initials = member.initials,
            containerColor =
                if (member.isCurrentUser) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            contentColor =
                if (member.isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(text = member.name, style = MaterialTheme.typography.titleSmall)
            if (member.isCurrentUser)
                Text(
                    text = stringResource(Res.string.details_you),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color =
                if (member.isOwner) LoomColors.positiveContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text =
                    stringResource(
                        if (member.isOwner) Res.string.details_owner else Res.string.details_member,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (member.isOwner) LoomColors.onPositiveContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.padding(
                        horizontal = LoomSpacing.small,
                        vertical = LoomSpacing.extraSmall,
                    ),
            )
        }
        trailingContent()
    }
}
