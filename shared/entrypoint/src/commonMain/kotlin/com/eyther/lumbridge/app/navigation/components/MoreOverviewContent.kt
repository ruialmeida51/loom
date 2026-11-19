package com.eyther.lumbridge.app.navigation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.layout.RootHeading
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.household_details
import com.eyther.lumbridge.shared.entrypoint.generated.resources.tab_more
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import org.jetbrains.compose.resources.stringResource

/** This is a navigation menu. Feature state and behavior stay in the destination's own feature. */
@Composable
internal fun MoreOverviewContent(
    onFeed: () -> Unit,
    onHouseholdDetails: () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(rootContentPadding())
                .testTag("more_content"),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        RootHeading(title = stringResource(Res.string.tab_more))
        GroupedSurface {
            NavigationRow(
                title = stringResource(Res.string.ui_feed),
                glyph = Glyph.FEED,
                onClick = onFeed,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            NavigationRow(
                title = stringResource(Res.string.household_details),
                glyph = Glyph.HOME,
                onClick = onHouseholdDetails,
            )
        }
    }
}
