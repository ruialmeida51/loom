package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.core.presentation.components.RefreshSkeletonContent
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RefreshSkeletonUiTest {
    @Test
    fun refreshShowsSkeletonsWithoutDestroyingTheFocusedDraft() =
        runSkikoComposeUiTest(size = Size(360f, 640f)) {
            var refreshing by mutableStateOf(false)
            setContent {
                LoomTheme(mode = ThemeMode.LIGHT) {
                    RefreshSkeletonContent(
                        isRefreshing = refreshing,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        var draft by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.testTag("draft"),
                        )
                    }
                }
            }
            onNodeWithTag("draft").performClick().performTextInput("Milk and bread")
            refreshing = true
            waitForIdle()
            onNodeWithTag("refresh_skeleton").assertExists()
            refreshing = false
            waitForIdle()
            onNodeWithTag("draft").assertTextContains("Milk and bread").assertIsFocused()
        }
}
