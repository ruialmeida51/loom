package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.details_member
import com.eyther.lumbridge.features.household.generated.resources.details_owner
import org.jetbrains.compose.resources.stringResource

/** Owner or Member, the same tag wherever a household member's role is shown. */
@Composable
fun MemberRoleChip(isOwner: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color =
            if (isOwner) LoomColors.positiveContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = stringResource(if (isOwner) Res.string.details_owner else Res.string.details_member),
            style = MaterialTheme.typography.labelMedium,
            color =
                if (isOwner) LoomColors.onPositiveContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(horizontal = LoomSpacing.small, vertical = LoomSpacing.extraSmall),
        )
    }
}
