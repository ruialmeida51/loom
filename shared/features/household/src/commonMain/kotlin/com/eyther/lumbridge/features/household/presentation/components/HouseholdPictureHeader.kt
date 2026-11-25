package com.eyther.lumbridge.features.household.presentation.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.LoomTonalButton
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.household_picture_add
import com.eyther.lumbridge.features.household.generated.resources.household_picture_change
import com.eyther.lumbridge.features.household.generated.resources.household_picture_description
import com.eyther.lumbridge.features.household.generated.resources.household_picture_remove
import org.jetbrains.compose.resources.stringResource

private val HeaderBadgeSize = 88.dp

/** The household's photo (or initial badge), its name, and the photo actions any member can use. */
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
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.testTag("household_picture").semantics { contentDescription = description },
        ) {
            HouseholdBadge(
                id = householdId,
                name = name,
                size = HeaderBadgeSize,
                picturePath = picturePath,
            )
            if (saving)
                CircularProgressIndicator(Modifier.size(HeaderBadgeSize * 0.5f), strokeWidth = 3.dp)
        }
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoomTonalButton(
                text =
                    stringResource(
                        if (picturePath != null) Res.string.household_picture_change
                        else Res.string.household_picture_add,
                    ),
                onClick = onChange,
                enabled = enabled,
                loading = saving,
                modifier = Modifier.testTag("change_household_picture"),
            )
            if (picturePath != null)
                LoomSecondaryButton(
                    text = stringResource(Res.string.household_picture_remove),
                    onClick = onRemove,
                    enabled = enabled,
                    modifier = Modifier.testTag("remove_household_picture"),
                )
        }
    }
}
