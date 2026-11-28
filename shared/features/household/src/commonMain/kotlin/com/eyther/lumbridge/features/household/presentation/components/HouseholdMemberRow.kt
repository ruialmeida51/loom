package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_you
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import org.jetbrains.compose.resources.stringResource

/** A member of the household; tapping the row opens their profile. */
@Composable
internal fun HouseholdMemberRow(
    member: HouseholdMemberUi,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier =
            Modifier.clickable(role = Role.Button, onClick = onClick)
                .padding(LoomSpacing.content)
                .testTag("member_${member.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        MemberAvatar(memberId = member.id, name = member.name, picturePath = member.avatarPath)
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
        MemberRoleChip(isOwner = member.isOwner)
        trailingContent()
    }
}
