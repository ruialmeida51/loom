package com.eyther.lumbridge.core.data

import android.content.ClipData
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual object PlatformExport {
    actual fun shareCsv(filename: String, content: String) {
        val context = PlatformStorage.applicationContext
        val directory = File(context.cacheDir, "statements").apply { mkdirs() }
        val file =
            File(directory, filename.replace(Regex("[^A-Za-z0-9._-]"), "_")).apply {
                writeText(content)
            }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("Monthly statement", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(
            Intent.createChooser(intent, "Share your statement")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
