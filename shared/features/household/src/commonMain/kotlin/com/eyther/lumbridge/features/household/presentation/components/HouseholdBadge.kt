package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HouseholdBadge(id: String, name: String) {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
    Surface(
        color = colors[id.hashCode().ushr(1) % colors.size],
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Text(
                name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString().orEmpty(),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
