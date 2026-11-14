package com.eyther.lumbridge.features.legal.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.generated.resources.Res
import com.eyther.lumbridge.features.legal.generated.resources.delete_account
import com.eyther.lumbridge.features.legal.generated.resources.privacy_policy
import com.eyther.lumbridge.features.legal.generated.resources.terms_of_use
import org.jetbrains.compose.resources.stringResource

@Composable
fun LegalLinks(
    onOpenDocument: (LegalDocumentDomain) -> Unit,
    showAccountDeletion: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onOpenDocument(LegalDocumentDomain.PRIVACY_POLICY) }) {
                Text(stringResource(Res.string.privacy_policy))
            }
            TextButton(onClick = { onOpenDocument(LegalDocumentDomain.TERMS) }) {
                Text(stringResource(Res.string.terms_of_use))
            }
        }
        if (showAccountDeletion) {
            TextButton(onClick = { onOpenDocument(LegalDocumentDomain.ACCOUNT_DELETION) }) {
                Text(stringResource(Res.string.delete_account))
            }
        }
    }
}
