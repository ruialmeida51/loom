package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingLine
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.presentation.components.FeedPreviewLoading

private val SectionHeaderHeight =
    48.dp // Match the title/action row while keeping the news rail below it.

/** Matches the three compact task previews instead of presenting an uncontained six-row list. */
@Composable
internal fun HomeTasksLoading() {
    GroupedSurface(modifier = Modifier.testTag("loading_rows")) {
        repeat(3) { index ->
            Column(
                Modifier.padding(LoomSpacing.content),
                verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
            ) {
                LoadingLine(widthFraction = .65f, style = MaterialTheme.typography.titleMedium)
                LoadingLine(widthFraction = .45f, style = MaterialTheme.typography.bodySmall)
            }
            if (index < 2) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
internal fun HomeShoppingLoading() {
    GroupedSurface(modifier = Modifier.testTag("loading_rows")) {
        Column(
            Modifier.padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            repeat(3) { LoadingLine(widthFraction = .6f) }
        }
    }
}

/** Uses the retained section's bounds, including empty cards, without moving the content below. */
@Composable
internal fun HomeRefreshLoading() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = LoomSpacing.content),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
    ) {
        Box(
            Modifier.fillMaxWidth().heightIn(min = SectionHeaderHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            LoadingLine(widthFraction = .4f, style = MaterialTheme.typography.titleLarge)
        }
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier.padding(LoomSpacing.content),
                verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            ) {
                repeat(3) { LoadingLine(widthFraction = .65f) }
            }
        }
    }
}

@Composable
internal fun HomeNewsRefreshLoading() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        Box(
            Modifier.fillMaxWidth()
                .padding(horizontal = LoomSpacing.content)
                .heightIn(min = SectionHeaderHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            LoadingLine(widthFraction = .4f, style = MaterialTheme.typography.titleLarge)
        }
        FeedPreviewLoading()
    }
}
