package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.CheckCircle
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_add_item
import org.jetbrains.compose.resources.stringResource

/** A stable input row: committing a line keeps this field focused and the keyboard open. */
@Composable
fun ShoppingEntryRow(
    value: String,
    enabled: Boolean,
    canSubmit: Boolean,
    onValue: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CheckCircle(
            checked = false,
            onCheckedChange = null,
            modifier = Modifier.size(48.dp).testTag("shopping_entry_checkbox"),
        )
        BasicTextField(
            value = value,
            onValueChange = onValue,
            enabled = enabled,
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions = KeyboardActions(onNext = { onSubmit() }, onDone = { onSubmit() }),
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { input ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty())
                            Text(
                                stringResource(Res.string.checklist_add_item),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        input()
                    }
                    IconButton(onClick = onSubmit, enabled = canSubmit) {
                        LoomIcon(glyph = Glyph.PLUS)
                    }
                }
            },
            modifier =
                Modifier.weight(1f).testTag("shopping_inline_input").onPreviewKeyEvent { event ->
                    if (
                        event.type == KeyEventType.KeyDown &&
                            (event.key == Key.Enter || event.key == Key.NumPadEnter)
                    ) {
                        onSubmit()
                        true
                    } else false
                },
        )
    }
}
