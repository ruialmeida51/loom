package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.extensions.shimmerReverse

/** Field-shaped placeholders, adapted from Plannot's FormSectionShimmer. */
@Composable
fun LoadingForm(modifier: Modifier = Modifier, fields: Int = 3) {
    Column(
        modifier = modifier.testTag("loading_form"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LoadingLine(widthFraction = .4f)
        repeat(fields) {
            Box(
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .shimmerReverse(true, RoundedCornerShape(12.dp)),
            )
        }
        LoadingRow()
    }
}
