package com.eyther.lumbridge.core.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberTextSharer(): (String) -> Boolean = remember {
    { text ->
        runCatching {
                var root = UIApplication.sharedApplication.keyWindow?.rootViewController
                while (root?.presentedViewController != null) root = root?.presentedViewController
                val presenter = root ?: return@runCatching false
                val controller =
                    UIActivityViewController(
                        activityItems = listOf(text),
                        applicationActivities = null,
                    )
                controller.popoverPresentationController?.sourceView = presenter.view
                presenter.presentViewController(controller, animated = true, completion = null)
                true
            }
            .getOrDefault(false)
    }
}
