package com.eyther.lumbridge.features.legal.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveButton
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.generated.resources.Res
import com.eyther.lumbridge.features.legal.generated.resources.delete_account
import com.eyther.lumbridge.features.legal.generated.resources.privacy_and_legal
import com.eyther.lumbridge.features.legal.generated.resources.privacy_introduction
import com.eyther.lumbridge.features.legal.generated.resources.privacy_policy
import com.eyther.lumbridge.features.legal.generated.resources.terms_of_use
import org.jetbrains.compose.resources.stringResource

@Composable
fun LegalLinks(
    onOpenDocument: (LegalDocumentDomain) -> Unit,
    showAccountDeletion: Boolean = false,
    showIntroduction: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showIntroduction) {
            SectionTitle(title = stringResource(Res.string.privacy_and_legal))
            Text(
                stringResource(Res.string.privacy_introduction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GroupedSurface {
            NavigationRow(
                title = stringResource(Res.string.privacy_policy),
                glyph = Glyph.SHIELD,
                onClick = { onOpenDocument(LegalDocumentDomain.PRIVACY_POLICY) },
            )
            LoomDivider()
            NavigationRow(
                title = stringResource(Res.string.terms_of_use),
                glyph = Glyph.FEED,
                onClick = { onOpenDocument(LegalDocumentDomain.TERMS) },
            )
        }
        if (showAccountDeletion) {
            DestructiveButton(
                text = stringResource(Res.string.delete_account),
                onClick = { onOpenDocument(LegalDocumentDomain.ACCOUNT_DELETION) },
            )
        }
    }
}
