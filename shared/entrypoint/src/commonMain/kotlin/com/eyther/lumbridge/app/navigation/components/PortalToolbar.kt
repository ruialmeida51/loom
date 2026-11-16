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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.extensions.shimmer
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_households
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_switch_household
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PortalToolbar(
    householdName: String?,
    nickname: String,
    loadingProfile: Boolean,
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
        SelectorButton(
            text = householdName ?: stringResource(Res.string.portal_households),
            onClick = onHousehold,
            textStyle = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier.weight(1f).testTag("household_switch").semantics {
                    contentDescription = householdLabel
                },
        )
        Surface(
            onClick = onAccount,
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier.weight(1f).heightIn(min = 48.dp).testTag("open_my_account").semantics {
                    contentDescription = accountLabel
                },
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        if (loadingProfile)
                            Box(
                                Modifier.size(36.dp)
                                    .shimmer(true, shape = CircleShape)
                                    .testTag("profile_avatar_loading"),
                            )
                        else
                            Text(
                                nickname.ifBlank { accountLabel }.take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.testTag("profile_avatar_initial"),
                            )
                    }
                }
            }
        }
    }
}
