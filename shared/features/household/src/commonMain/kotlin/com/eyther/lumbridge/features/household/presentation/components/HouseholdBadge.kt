package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.picture.RemotePicture
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSquircle

/**
 * A household's mark: its initial on a squircle in a colour derived from its id, so each home is
 * recognisable in the toolbar and the chooser. Its photo, when set, replaces the initial.
 */
@Composable
fun HouseholdBadge(
    id: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    picturePath: String? = null,
) {
    RemotePicture(path = picturePath, shape = LoomSquircle, modifier = modifier.size(size)) {
        InitialBadge(id = id, name = name, size = size)
    }
}

@Composable
private fun InitialBadge(id: String, name: String, size: Dp) {
    Surface(color = LoomColors.member(id), contentColor = LoomColors.onMember, shape = LoomSquircle) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                householdInitial(name),
                style =
                    if (size >= 44.dp) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val LeadingArticles = setOf("the", "a", "an")

/** "The little nest" → "L": skip a leading article so similar names stay distinguishable. */
internal fun householdInitial(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val word = words.firstOrNull { it.lowercase() !in LeadingArticles } ?: words.firstOrNull()
    return word?.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString().orEmpty()
}
