package com.eyther.lumbridge.features.insights.data.export

import android.content.ClipData
import android.content.Intent
import androidx.core.content.FileProvider
import com.eyther.lumbridge.core.data.PlatformStorage
import java.io.File

actual object PlatformExport {
    actual fun shareCsv(filename: String, content: String, title: String) {
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
                clipData = ClipData.newRawUri(title, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(
            Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
