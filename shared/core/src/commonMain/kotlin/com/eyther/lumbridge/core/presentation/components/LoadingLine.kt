package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import com.eyther.lumbridge.core.presentation.extensions.shimmer

/** Adapted from Plannot's grouped loading lines; animation is read only during drawing. */
@Composable
fun LoadingLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val height = with(LocalDensity.current) { style.lineHeight.toDp() }
    Box(modifier.fillMaxWidth(widthFraction).height(height).clearAndSetSemantics {}.shimmer(true))
}
