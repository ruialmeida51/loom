package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.member_selector_choose
import com.eyther.lumbridge.features.household.generated.resources.member_selector_you
import org.jetbrains.compose.resources.stringResource

/** Compact picker row with ID-based selection, even when two members share a nickname. */
@Composable
fun MemberPickerRow(
    label: String,
    members: List<MemberDomain>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentUserId: String? = null,
    emphasisColor: Color = Color.Unspecified,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = members.firstOrNull { it.id == selectedId }
    val you = stringResource(Res.string.member_selector_you)
    Box(modifier = modifier.fillMaxWidth()) {
        NavigationRow(
            title =
                if (selected != null && selected.id == currentUserId) you
                else selected?.name ?: stringResource(Res.string.member_selector_choose),
            description = label,
            glyph = Glyph.PERSON,
            trailingGlyph = Glyph.CHEVRON_DOWN,
            onClick = { expanded = true },
            enabled = enabled,
            emphasisColor = emphasisColor,
        )
        DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            members.forEach { member ->
                DropdownMenuItem(
                    text = { Text(if (member.id == currentUserId) you else member.name) },
                    onClick = {
                        expanded = false
                        onSelected(member.id)
                    },
                )
            }
        }
    }
}
