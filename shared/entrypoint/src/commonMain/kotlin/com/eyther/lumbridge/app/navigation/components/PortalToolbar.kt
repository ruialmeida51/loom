package com.eyther.lumbridge.app.navigation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.extensions.shimmer
import com.eyther.lumbridge.features.household.presentation.components.HouseholdBadge
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_households
import com.eyther.lumbridge.shared.entrypoint.generated.resources.portal_my_account
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_switch_household
import org.jetbrains.compose.resources.stringResource

private val AvatarSize = 36.dp

/**
 * The tab roots' toolbar, after Plannot's tenant switcher: the household's badge and name open the
 * household chooser; the user's avatar, in their member colour, opens My account.
 */
@Composable
internal fun PortalToolbar(
    householdId: String?,
    householdName: String?,
    householdPicturePath: String?,
    userId: String,
    avatarPath: String?,
    nickname: String,
    loadingProfile: Boolean,
    onHousehold: () -> Unit,
    onAccount: () -> Unit,
) {
    val name = householdName ?: stringResource(Res.string.portal_households)
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
                .heightIn(min = 60.dp)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier.weight(1f, fill = false)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button, onClick = onHousehold)
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .testTag("household_switch")
                    .semantics { contentDescription = householdLabel },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HouseholdBadge(
                id = householdId.orEmpty(),
                name = name,
                size = 34.dp,
                picturePath = householdPicturePath,
            )
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            LoomIcon(
                glyph = Glyph.CHEVRON_DOWN,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                decorative = true,
                modifier = Modifier.size(18.dp),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onAccount)
                    .testTag("open_my_account")
                    .semantics { contentDescription = accountLabel },
        ) {
            if (loadingProfile)
                Box(
                    Modifier.size(AvatarSize)
                        .shimmer(true, shape = CircleShape)
                        .testTag("profile_avatar_loading"),
                )
            else {
                val initial = nickname.ifBlank { accountLabel }.take(1).uppercase()
                Box(
                    Modifier.testTag("profile_avatar_initial").semantics {
                        text = AnnotatedString(initial)
                    },
                ) {
                    MemberAvatar(
                        memberId = userId,
                        initials = initial,
                        picturePath = avatarPath,
                        modifier = Modifier.size(AvatarSize),
                    )
                }
            }
        }
    }
}
