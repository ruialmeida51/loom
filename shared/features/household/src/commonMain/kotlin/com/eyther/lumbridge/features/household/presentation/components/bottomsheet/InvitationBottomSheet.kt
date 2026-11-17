package com.eyther.lumbridge.features.household.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.invitation_expiry
import com.eyther.lumbridge.features.household.generated.resources.invitation_revoke
import com.eyther.lumbridge.features.household.generated.resources.invitation_short_description
import com.eyther.lumbridge.features.household.generated.resources.ui_close
import com.eyther.lumbridge.features.household.generated.resources.ui_invite_someone_home
import org.jetbrains.compose.resources.stringResource

private val CodeLetterSpacing = 4.sp // Six readable characters without stretching the whole sheet.

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
        onAction = onClose,
        actionLabel = stringResource(Res.string.ui_close),
        busy = busy,
    ) {
        Text(
            text = stringResource(Res.string.invitation_short_description),
            style = MaterialTheme.typography.bodyMedium,
        )
        GroupedSurface {
            Box(
                modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
                contentAlignment = Alignment.Center,
            ) {
                SelectionContainer {
                    Text(
                        text = invitation,
                        style =
                            MaterialTheme.typography.headlineMedium.copy(
                                letterSpacing = CodeLetterSpacing,
                            ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Text(
            text = stringResource(Res.string.invitation_expiry),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
        GroupedSurface {
            NavigationRow(
                title = stringResource(Res.string.invitation_revoke),
                glyph = Glyph.DELETE,
                emphasisColor = MaterialTheme.colorScheme.error,
                enabled = !busy,
                onClick = onRevoke,
            )
        }
    }
}
