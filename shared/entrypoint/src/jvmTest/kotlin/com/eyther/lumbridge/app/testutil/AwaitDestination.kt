package com.eyther.lumbridge.app.testutil

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitLoadedDestination(tag: String) {
    waitUntil(timeoutMillis = 5_000) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() &&
            onAllNodes(
                    (hasTestTag("loading_rows") or hasTestTag("loading_form")) and
                        hasAnyAncestor(hasTestTag(tag)),
                )
                .fetchSemanticsNodes()
                .isEmpty()
    }
    waitForIdle()
}
