package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingLine
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/** Mirrors the payment summary and metadata geometry, including the gap between cards. */
@Composable
internal fun PaymentDetailsLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.content)) {
        LoomCard {
            LoadingLine(widthFraction = .4f, style = MaterialTheme.typography.labelMedium)
            LoadingLine(widthFraction = .7f, style = MaterialTheme.typography.titleLarge)
            LoadingLine(widthFraction = .5f, style = MaterialTheme.typography.displaySmall)
        }
        GroupedSurface(modifier = Modifier.testTag("loading_rows")) {
            repeat(4) { index ->
                if (index > 0) LoomDivider()
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal = LoomSpacing.content,
                                vertical = LoomSpacing.medium,
                            ),
                    horizontalArrangement = Arrangement.spacedBy(LoomSpacing.content),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        LoadingLine(
                            widthFraction = .5f,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        LoadingLine(
                            widthFraction = .8f,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }
    }
}
