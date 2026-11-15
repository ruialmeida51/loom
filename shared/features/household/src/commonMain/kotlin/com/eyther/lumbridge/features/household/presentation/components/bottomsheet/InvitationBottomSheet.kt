package com.eyther.lumbridge.features.household.presentation.components.bottomsheet

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.ui_done
import com.eyther.lumbridge.features.household.generated.resources.ui_invite_someone_home
import com.eyther.lumbridge.features.household.generated.resources.ui_one_time_invitation_expires_in_48_hours
import org.jetbrains.compose.resources.stringResource

@Composable
fun InvitationBottomSheet(invitation: String, onClose: () -> Unit) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_invite_someone_home),
        action = { TextButton(onClick = onClose) { Text(stringResource(Res.string.ui_done)) } },
        dismissAction = {},
    ) {
        Text(
            text = stringResource(Res.string.ui_one_time_invitation_expires_in_48_hours),
            style = MaterialTheme.typography.bodyMedium,
        )
        SelectionContainer { Text(text = invitation, style = MaterialTheme.typography.bodyLarge) }
    }
}
