package com.eyther.lumbridge.features.insights.data.export

import com.eyther.lumbridge.features.insights.domain.errors.InsightsError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
actual object PlatformExport {
    actual fun shareCsv(filename: String, content: String, title: String) {
        val safeName = filename.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val path = NSTemporaryDirectory() + safeName
        val written = (content as NSString).writeToFile(
            path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        if (!written) throw InsightsError.ExportFailed()
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: throw InsightsError.ExportFailed()
        val controller =
            UIActivityViewController(
                activityItems = listOf(NSURL.fileURLWithPath(path)),
                applicationActivities = null,
            )
        controller.popoverPresentationController?.sourceView = root.view
        root.presentViewController(controller, animated = true, completion = null)
    }
}
