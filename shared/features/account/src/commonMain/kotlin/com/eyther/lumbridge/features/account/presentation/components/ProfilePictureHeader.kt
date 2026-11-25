package com.eyther.lumbridge.features.account.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.LoomTonalButton
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.picture_add
import com.eyther.lumbridge.features.account.generated.resources.picture_change
import com.eyther.lumbridge.features.account.generated.resources.picture_description
import com.eyther.lumbridge.features.account.generated.resources.picture_remove
import org.jetbrains.compose.resources.stringResource

private val HeaderAvatarSize = 88.dp

/** The user's photo (or coloured initials), their name, and the photo actions. */
@Composable
internal fun ProfilePictureHeader(
    userId: String,
    nickname: String,
    avatarPath: String?,
    hasPicture: Boolean,
    enabled: Boolean,
    saving: Boolean,
    onChange: () -> Unit,
    onRemove: () -> Unit,
) {
    val description = stringResource(Res.string.picture_description)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag("profile_picture").semantics { contentDescription = description },
        ) {
            MemberAvatar(
                memberId = userId,
                initials = nickname.take(1).uppercase(),
                picturePath = avatarPath,
                modifier = Modifier.size(HeaderAvatarSize),
            )
            if (saving)
                CircularProgressIndicator(
                    modifier = Modifier.size(HeaderAvatarSize),
                    strokeWidth = 3.dp,
                )
        }
        Text(nickname, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoomTonalButton(
                text =
                    stringResource(if (hasPicture) Res.string.picture_change else Res.string.picture_add),
                onClick = onChange,
                enabled = enabled,
                loading = saving,
                modifier = Modifier.testTag("change_profile_picture"),
            )
            if (hasPicture)
                LoomSecondaryButton(
                    text = stringResource(Res.string.picture_remove),
                    onClick = onRemove,
                    enabled = enabled,
                    modifier = Modifier.testTag("remove_profile_picture"),
                )
        }
    }
}
