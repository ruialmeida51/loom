package com.eyther.lumbridge.app.navigation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_households
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_switch_household
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PortalToolbar(
    householdName: String?,
    nickname: String,
    onHousehold: () -> Unit,
    onAccount: () -> Unit,
) {
    val householdLabel =
        householdName?.let { stringResource(Res.string.toolbar_switch_household, it) }
            ?: stringResource(Res.string.portal_households)
    val accountLabel = stringResource(Res.string.portal_my_account)
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                )
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onHousehold,
            modifier =
                Modifier.weight(1f).testTag("household_switch").semantics {
                    contentDescription = householdLabel
                },
        ) {
            Text(
                text = householdName ?: stringResource(Res.string.portal_households),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(text = "⌄", modifier = Modifier.padding(start = 8.dp))
        }
        IconButton(
            onClick = onAccount,
            modifier =
                Modifier.testTag("open_my_account").semantics { contentDescription = accountLabel },
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text(
                        nickname.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.testTag("profile_avatar_initial"),
                    )
                }
            }
        }
    }
}
