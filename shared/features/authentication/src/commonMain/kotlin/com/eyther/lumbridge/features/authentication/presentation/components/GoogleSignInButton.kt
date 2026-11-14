package com.eyther.lumbridge.features.authentication.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.google_continue
import com.eyther.lumbridge.features.authentication.generated.resources.google_mark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoogleSignInButton(onClick: () -> Unit, loading: Boolean, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Image(
                    painter = painterResource(Res.drawable.google_mark),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(stringResource(Res.string.google_continue))
        }
    }
}
