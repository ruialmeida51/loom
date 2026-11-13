package com.eyther.lumbridge.features.insights.data.export

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual object PlatformExport {
    actual fun shareCsv(filename: String, content: String) {
        val dialog = FileDialog(null as Frame?, "Save monthly statement", FileDialog.SAVE)
        dialog.file = filename
        dialog.isVisible = true
        if (dialog.file != null) File(dialog.directory, dialog.file).writeText(content)
        dialog.dispose()
    }
}
