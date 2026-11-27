package com.eyther.lumbridge.features.account.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon

/**
 * A preference picked from a short menu, showing the current choice. Only the menu's expansion is
 * transient; the selected value remains ViewModel-owned. Tags are `<tag>_preference`,
 * `<tag>_menu_anchor` and `<tag>_menu`.
 */
@Composable
internal fun <T> MenuPreferenceRow(
    title: String,
    glyph: Glyph,
    choices: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit,
    tag: String,
) {
    var expanded by remember { mutableStateOf(false) }
    NavigationRow(
        title = title,
        value = label(selected),
        glyph = glyph,
        onClick = { expanded = true },
        enabled = enabled,
        modifier = Modifier.testTag("${tag}_preference"),
        trailingContent = {
            Box(modifier = Modifier.testTag("${tag}_menu_anchor")) {
                LoomIcon(glyph = Glyph.CHEVRON_DOWN, decorative = true)
                DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.testTag("${tag}_menu"),
                ) {
                    choices.forEach { choice ->
                        DropdownMenuItem(
                            modifier = Modifier.semantics { this.selected = selected == choice },
                            text = { Text(label(choice)) },
                            trailingIcon = {
                                if (selected == choice)
                                    LoomIcon(glyph = Glyph.CHECK, decorative = true)
                            },
                            onClick = {
                                expanded = false
                                onSelected(choice)
                            },
                        )
                    }
                }
            }
        },
    )
}
