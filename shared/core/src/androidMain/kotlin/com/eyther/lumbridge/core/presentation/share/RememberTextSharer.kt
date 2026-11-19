package com.eyther.lumbridge.core.presentation.share

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberTextSharer(): (String) -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            runCatching {
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                context.startActivity(Intent.createChooser(intent, null))
            }
                .isSuccess
        }
    }
}
