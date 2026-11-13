package com.eyther.lumbridge.features.insights.data.export

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
actual object PlatformExport {
    actual fun shareCsv(filename: String, content: String) {
        val safeName = filename.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val path = NSTemporaryDirectory() + safeName
        (content as NSString).writeToFile(
            path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val controller =
            UIActivityViewController(
                activityItems = listOf(NSURL.fileURLWithPath(path)),
                applicationActivities = null,
            )
        controller.popoverPresentationController?.sourceView = root.view
        root.presentViewController(controller, animated = true, completion = null)
    }
}
