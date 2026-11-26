package com.eyther.lumbridge.core.presentation.components.picture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.picture_choose
import com.eyther.lumbridge.core.generated.resources.picture_remove
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import org.jetbrains.compose.resources.stringResource

private val PencilBadgeSize = 30.dp

/**
 * A photo the user can change: tapping it opens the photo library ([onChoose]); a pencil in the
 * bottom-right corner says so. With a photo set ([onRemove] non-null) the tap offers Change or
 * Remove instead. [saving] shows progress over the photo.
 */
@Composable
fun EditablePicture(
    description: String,
    shape: Shape,
    size: Dp,
    enabled: Boolean,
    saving: Boolean,
    onChoose: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val open: () -> Unit = {
        if (onRemove == null) onChoose() else menuOpen = true
    }
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(size)
                    .clip(shape)
                    .clickable(enabled = enabled && !saving, role = Role.Button, onClick = open)
                    .semantics { contentDescription = description },
        ) {
            content()
            if (saving)
                CircularProgressIndicator(Modifier.size(size * 0.45f), strokeWidth = 3.dp)
        }
        // A Surface takes the touches over it, so the badge itself must open the picker too.
        Surface(
            onClick = open,
            enabled = enabled && !saving,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .size(PencilBadgeSize)
                    .testTag("picture_edit_badge")
                    .clearAndSetSemantics {},
        ) {
            LoomIcon(
                glyph = Glyph.EDIT,
                tint = MaterialTheme.colorScheme.onPrimary,
                decorative = true,
                modifier = Modifier.padding(7.dp),
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.picture_choose)) },
                onClick = {
                    menuOpen = false
                    onChoose()
                },
                modifier = Modifier.testTag("picture_choose"),
            )
            onRemove?.let { remove ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.picture_remove),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuOpen = false
                        remove()
                    },
                    modifier = Modifier.testTag("picture_remove"),
                )
            }
        }
    }
}
