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
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

/** Compact person selection; the owning form keeps the selected ID, including duplicate names. */
@Composable
fun MemberPickerRow(
    label: String,
    members: List<MemberDomain>,
    selectedId: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        NavigationRow(
            title = label,
            value = members.firstOrNull { it.id == selectedId }?.name.orEmpty(),
            enabled = enabled,
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            members.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member.name) },
                    onClick = {
                        expanded = false
                        onSelected(member.id)
                    },
                )
            }
        }
    }
}
