package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.picture.EditablePicture
import com.eyther.lumbridge.core.presentation.theme.LoomSquircle
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.household_picture_description
import org.jetbrains.compose.resources.stringResource

private val HeaderBadgeSize = 88.dp

/** The household's photo (or initial badge) with a pencil any member can use, and its name. */
@Composable
internal fun HouseholdPictureHeader(
    householdId: String,
    name: String,
    picturePath: String?,
    enabled: Boolean,
    saving: Boolean,
    onChange: () -> Unit,
    onRemove: () -> Unit,
) {
    val description = stringResource(Res.string.household_picture_description, name)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EditablePicture(
            description = description,
            shape = LoomSquircle,
            size = HeaderBadgeSize,
            enabled = enabled,
            saving = saving,
            onChoose = onChange,
            onRemove = if (picturePath != null) onRemove else null,
            modifier = Modifier.testTag("household_picture"),
        ) {
            HouseholdBadge(
                id = householdId,
                name = name,
                size = HeaderBadgeSize,
                picturePath = picturePath,
            )
        }
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
