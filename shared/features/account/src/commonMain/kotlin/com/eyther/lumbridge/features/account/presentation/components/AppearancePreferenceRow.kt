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
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.appearance_title
import com.eyther.lumbridge.features.account.presentation.mapper.themeLabel
import org.jetbrains.compose.resources.stringResource

/** Only the menu's expansion is transient; the selected preference remains ViewModel-owned. */
@Composable
internal fun AppearancePreferenceRow(
    theme: ThemeMode,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    NavigationRow(
        title = stringResource(Res.string.appearance_title),
        value = themeLabel(theme),
        glyph = Glyph.SETTINGS,
        onClick = { expanded = true },
        enabled = enabled,
        modifier = Modifier.testTag("appearance_preference"),
        trailingContent = {
            Box(modifier = Modifier.testTag("appearance_menu_anchor")) {
                LoomIcon(glyph = Glyph.CHEVRON_DOWN, decorative = true)
                DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.testTag("appearance_menu"),
                ) {
                    ThemeMode.entries.forEach { choice ->
                        DropdownMenuItem(
                            modifier = Modifier.semantics { selected = theme == choice },
                            text = { Text(themeLabel(choice)) },
                            trailingIcon = {
                                if (theme == choice)
                                    LoomIcon(glyph = Glyph.CHECK, decorative = true)
                            },
                            onClick = {
                                expanded = false
                                onSelected(choice.name)
                            },
                        )
                    }
                }
            }
        },
    )
}
