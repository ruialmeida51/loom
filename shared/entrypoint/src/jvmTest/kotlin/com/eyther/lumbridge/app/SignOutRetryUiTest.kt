package com.eyther.lumbridge.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.DemoPortalHost
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class SignOutRetryUiTest {
    @Test
    fun signOutStaysConfirmedWhileBusyAndKeepsTheConfirmationOnFailure() =
        runSkikoComposeUiTest(size = Size(390f, 820f)) {
            val store = DemoHouseholdStore(DemoHouseholdStore.households.first())
            var signingOut by mutableStateOf(false)
            var signOutError by mutableStateOf<TextResource?>(null)
            var calls = 0
            setContent {
                LoomTheme(ThemeMode.LIGHT) {
                    DemoPortalHost(
                        stores = mapOf(store.context.id to store),
                        selectedId = store.context.id,
                        onSelect = {},
                        onSignOut = {
                            calls++
                            signingOut = true
                            signOutError = null
                        },
                        signingOut = signingOut,
                        signOutError = signOutError,
                        onSignOutDismissed = { signOutError = null },
                    )
                }
            }
            awaitLoadedDestination("home_content")
            onNodeWithTag("open_my_account").performClick()
            awaitLoadedDestination("account_content")
            onNodeWithTag("account_content").performScrollToNode(hasTestTag("sign_out"))
            onNodeWithTag("sign_out").performClick()
            onNodeWithTag("confirm_sign_out").performClick()
            onNodeWithTag("confirm_sign_out").assertIsNotEnabled()
            onNodeWithText("Cancel").assertIsNotEnabled()
            assertEquals(1, calls)
            runOnIdle {
                signingOut = false
                signOutError = CommonError.NoConnection().toTextResource()
            }
            onNodeWithTag("confirm_sign_out").assertExists().assertIsEnabled()
            onNodeWithTag("account_content").assertExists()
            onNodeWithText("Cancel").performClick()
            onNodeWithTag("confirm_sign_out").assertDoesNotExist()
            assertNull(signOutError)
            assertEquals(1, calls)
        }
}
