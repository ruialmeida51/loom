package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.picture.RemotePicture
import com.eyther.lumbridge.core.presentation.theme.LoomColors

private val AvatarSize =
    32.dp // Compact identity cue; the adjacent name carries accessibility text.

// From this size an avatar is the screen's picture of someone, with initials to match.
private val LargeAvatarSize = 64.dp

/**
 * The initial an avatar shows for [name]: its first letter or digit, so quotes and emoji are
 * skipped; empty for a missing name, which shows a person glyph instead.
 */
fun memberInitial(name: String?): String =
    name?.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString().orEmpty()

/** Decorative identity cue beside a readable name; features choose any semantic emphasis. */
@Composable
fun PersonAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: Dp = AvatarSize,
) {
    Surface(
        modifier = modifier.size(size).clearAndSetSemantics {},
        shape = CircleShape,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (initials.isBlank())
                LoomIcon(glyph = Glyph.PERSON, tint = contentColor, decorative = true)
            else
                Text(
                    text = initials,
                    style =
                        if (size >= LargeAvatarSize) MaterialTheme.typography.displaySmall
                        else MaterialTheme.typography.labelMedium,
                    color = contentColor,
                )
        }
    }
}

/**
 * A person wherever they appear: their photo, or their initial on their stable household colour
 * ([memberInitial] of [name]; a person glyph when the name is unknown). [size] is the small
 * list size by default; headers pass a larger one.
 */
@Composable
fun MemberAvatar(
    memberId: String,
    name: String?,
    modifier: Modifier = Modifier,
    picturePath: String? = null,
    size: Dp = AvatarSize,
) {
    // The stored photo, when there is one, replaces the coloured initials.
    RemotePicture(path = picturePath, shape = CircleShape, modifier = modifier.size(size)) {
        PersonAvatar(
            initials = memberInitial(name),
            size = size,
            modifier = Modifier.fillMaxSize(),
            containerColor = LoomColors.member(memberId),
            contentColor = LoomColors.onMember,
        )
    }
}
