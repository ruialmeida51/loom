package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon

private val AvatarSize =
    32.dp // Compact identity cue; the adjacent name carries accessibility text.

/** Decorative identity cue beside a readable name; features choose any semantic emphasis. */
@Composable
fun PersonAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        modifier = modifier.size(AvatarSize).clearAndSetSemantics {},
        shape = CircleShape,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (initials.isBlank())
                LoomIcon(glyph = Glyph.PERSON, tint = contentColor, decorative = true)
            else
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                )
        }
    }
}
