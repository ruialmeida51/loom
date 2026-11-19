package com.eyther.lumbridge.features.authentication.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.google_continue
import com.eyther.lumbridge.features.authentication.generated.resources.google_mark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoogleSignInButton(onClick: () -> Unit, loading: Boolean, enabled: Boolean) {
    LoomSecondaryButton(
        text = stringResource(Res.string.google_continue),
        onClick = onClick,
        enabled = enabled,
        loading = loading,
        leadingContent = {
            Image(
                painter = painterResource(Res.drawable.google_mark),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}
