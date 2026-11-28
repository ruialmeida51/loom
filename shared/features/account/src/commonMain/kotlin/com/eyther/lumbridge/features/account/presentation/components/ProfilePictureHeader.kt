package com.eyther.lumbridge.features.account.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.picture.EditablePicture
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.picture_description
import org.jetbrains.compose.resources.stringResource

private val HeaderAvatarSize = 88.dp

/** The user's photo (or coloured initials) with a pencil to change it, and their name. */
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
        EditablePicture(
            description = description,
            shape = CircleShape,
            size = HeaderAvatarSize,
            enabled = enabled,
            saving = saving,
            onChoose = onChange,
            onRemove = if (hasPicture) onRemove else null,
            modifier = Modifier.testTag("profile_picture"),
        ) {
            MemberAvatar(
                memberId = userId,
                name = nickname,
                picturePath = avatarPath,
                size = HeaderAvatarSize,
            )
        }
        Text(nickname, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}
