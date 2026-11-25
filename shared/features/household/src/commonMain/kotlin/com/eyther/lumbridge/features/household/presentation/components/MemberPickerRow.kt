package com.eyther.lumbridge.features.household.presentation.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
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
    /** A short qualifier shown after a name, such as "Guest" for people outside the household. */
    qualifier: (MemberDomain) -> String? = { null },
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMember = members.firstOrNull { it.id == selectedId }
    val you = stringResource(Res.string.member_selector_you)
    NavigationRow(
        title =
            if (selectedMember != null && selectedMember.id == currentUserId) you
            else
                selectedMember?.let { member ->
                    qualifier(member)?.let { "${member.name} · $it" } ?: member.name
                } ?: stringResource(Res.string.member_selector_choose),
        description = label,
        glyph = Glyph.PERSON,
        onClick = { expanded = true },
        enabled = enabled,
        emphasisColor = emphasisColor,
        modifier = modifier,
        trailingContent = {
            Box(modifier = Modifier.testTag("member_menu_anchor")) {
                LoomIcon(glyph = Glyph.CHEVRON_DOWN, decorative = true)
                DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.testTag("member_menu"),
                ) {
                    members.forEach { member ->
                        DropdownMenuItem(
                            modifier = Modifier.semantics { selected = member.id == selectedId },
                            text = {
                                val name = if (member.id == currentUserId) you else member.name
                                Text(qualifier(member)?.let { "$name · $it" } ?: name)
                            },
                            trailingIcon = {
                                if (member.id == selectedId)
                                    LoomIcon(glyph = Glyph.CHECK, decorative = true)
                            },
                            onClick = {
                                expanded = false
                                onSelected(member.id)
                            },
                        )
                    }
                }
            }
        },
    )
}
