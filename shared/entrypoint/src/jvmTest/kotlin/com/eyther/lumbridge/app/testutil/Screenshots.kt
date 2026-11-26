package com.eyther.lumbridge.app.testutil

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import java.io.File
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Saves a PNG of the screen, or of the node tagged [tag], as `<name>.png` in the screenshots
 * directory (`loom.screenshots.dir`, by default `build/screenshots`) for review and CI artifacts.
 */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.capture(name: String, tag: String? = null) {
    waitForIdle()
    // Without a tag, the whole window: a sheet or popup adds its own root beside the screen's.
    val image =
        if (tag != null) onNodeWithTag(tag).assertIsDisplayed().captureToImage()
        else (this as? SkikoComposeUiTest)?.captureToImage() ?: onAllNodes(isRoot())[0].captureToImage()
    val directory =
        File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
    File(directory, "$name.png")
        .writeBytes(
            Image.makeFromBitmap(image.asSkiaBitmap())
                .encodeToData(EncodedImageFormat.PNG)!!
                .bytes,
        )
}
