package com.eyther.lumbridge.features.household.presentation.components.bottomsheet

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.DestructiveButton
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke
import com.eyther.lumbridge.features.household.generated.resources.invitation_short_description
import com.eyther.lumbridge.features.household.generated.resources.ui_done
import com.eyther.lumbridge.features.household.generated.resources.ui_invite_someone_home
import org.jetbrains.compose.resources.stringResource

@Composable
fun InvitationBottomSheet(
    invitation: String,
    busy: Boolean,
    error: TextResource?,
    onRevoke: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_invite_someone_home),
        action = {
            LoomButton(
                text = stringResource(Res.string.ui_done),
                onClick = onClose,
                enabled = !busy,
            )
        },
        dismissAction = {},
    ) {
        Text(
            text = stringResource(Res.string.invitation_short_description),
            style = MaterialTheme.typography.bodyMedium,
        )
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
        DestructiveButton(
            text = stringResource(Res.string.invitation_revoke),
            onClick = onRevoke,
            loading = busy,
        )
        SelectionContainer {
            Text(text = invitation, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
