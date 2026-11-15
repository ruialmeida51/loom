package com.eyther.lumbridge.core.presentation.components.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.ic_arrow_back
import com.eyther.lumbridge.core.generated.resources.toolbar_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun Toolbar(
    text: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    windowInsets: WindowInsets =
        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    backEnabled: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackButton) {
            IconButton(onClick = onBack, enabled = backEnabled) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.toolbar_back),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
    }
}
