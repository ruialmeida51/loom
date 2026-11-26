package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.eyther.lumbridge.core.presentation.components.LoadingLine
import com.eyther.lumbridge.core.presentation.extensions.shimmer
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Matches the article preview's viewport and text hierarchy without reshaping the page on arrival.
 */
@Composable
fun FeedPreviewLoading() {
    LazyRow(
        modifier = Modifier.fillMaxWidth().testTag("loading_rows"),
        contentPadding = PaddingValues(horizontal = LoomSpacing.content),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        items(2) {
            Surface(
                modifier = Modifier.width(FeedPreviewWidth),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column {
                    Box(
                        Modifier.fillMaxWidth()
                            .height(ArticlePictureHeight)
                            .clearAndSetSemantics {}
                            .shimmer(shouldShimmer = true, shape = RectangleShape)
                    )
                    Column(
                        Modifier.padding(LoomSpacing.content),
                        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                    ) {
                        LoadingLine(widthFraction = .5f, style = MaterialTheme.typography.labelMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall)) {
                            repeat(FeedPreviewTitleLines) {
                                LoadingLine(style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        LoadingLine(widthFraction = .6f, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
