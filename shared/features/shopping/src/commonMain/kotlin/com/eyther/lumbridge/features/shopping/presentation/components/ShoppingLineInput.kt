package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_item_name
import org.jetbrains.compose.resources.stringResource

/**
 * A single checklist line edited in place. It takes focus when it appears, with the cursor at the
 * end; the keyboard's next action (or Enter) continues below, and leaving the line finishes it.
 * Backspace in an already empty line calls [onDeleteEmpty], as Google Keep removes the line.
 */
@Composable
internal fun ShoppingLineInput(
    value: String,
    onValue: (String) -> Unit,
    onNext: () -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    onDeleteEmpty: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var focused by remember { mutableStateOf(false) }
    // The ViewModel owns the text; keep the local selection only while the texts agree.
    val shown = if (field.text == value) field else TextFieldValue(value, TextRange(value.length))
    val label = stringResource(Res.string.ui_item_name)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BasicTextField(
        value = shown,
        onValueChange = { next ->
            field = next
            if (next.text != value) onValue(next.text)
        },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
        textStyle =
            MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = textDecoration,
            ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { input ->
            Box {
                if (shown.text.isEmpty() && placeholder != null)
                    Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                input()
            }
        },
        modifier =
            modifier
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (focused && !state.isFocused) onFocusLost()
                    focused = state.isFocused
                }
                .onPreviewKeyEvent { event ->
                    if (
                        event.type == KeyEventType.KeyDown &&
                            (event.key == Key.Enter || event.key == Key.NumPadEnter)
                    ) {
                        onNext()
                        true
                    } else if (
                        onDeleteEmpty != null &&
                            event.type == KeyEventType.KeyDown &&
                            event.key == Key.Backspace &&
                            shown.text.isEmpty()
                    ) {
                        onDeleteEmpty()
                        true
                    } else false
                }
                .semantics { contentDescription = label },
    )
}
