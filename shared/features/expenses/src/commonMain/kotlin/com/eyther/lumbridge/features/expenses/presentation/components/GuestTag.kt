package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.features.expenses.domain.model.isGuest
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.guest_tag
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import org.jetbrains.compose.resources.stringResource

/** Marks someone outside the household wherever money UI names them. */
@Composable
fun GuestTag(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.guest_tag),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** A participant's name, followed by [GuestTag] when they are a guest. */
@Composable
fun NameWithGuestTag(
    name: String,
    isGuest: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = name,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (isGuest) GuestTag()
    }
}

/** Labels guests in member pickers, which show plain text rather than a tag. */
@Composable
fun guestQualifier(): (MemberDomain) -> String? {
    val guest = stringResource(Res.string.guest_tag)
    return { member -> if (member.isGuest) guest else null }
}
