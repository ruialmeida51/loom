package com.eyther.lumbridge.app.authentication

import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.app.authentication.data.SessionRecoveryFixtureViewModel
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.authentication.presentation.screen.SessionScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.cancel

@OptIn(ExperimentalTestApi::class)
class SessionRecoveryUiTest {
    @Test
    fun reconnectingStartupOffersOneRetryAndOnlyConfirmedSignOutShowsLogin() =
        runSkikoComposeUiTest(size = Size(360f, 780f)) {
            val viewModel = SessionRecoveryFixtureViewModel()
            try {
                setContent {
                    LoomTheme(ThemeMode.DARK) {
                        SessionScreen(
                            viewModel = viewModel,
                            signedOutContent = { Text("Login surface") },
                            authenticatedContent = { _, _, _, _ -> Text("Household surface") },
                        )
                    }
                }
                onNodeWithText("Login surface").assertDoesNotExist()
                onNodeWithTag("retry_session").assertDoesNotExist()
                runOnIdle {
                    viewModel.state.value =
                        SessionState(
                            reconnecting = true,
                            sessionRecoveryAvailable = true,
                        )
                }
                onNodeWithTag("retry_session").assertIsDisplayed().performClick()
                onNodeWithTag("retry_session").assertIsNotEnabled()
                onNodeWithText("Choose a household").assertDoesNotExist()
                assertEquals(1, viewModel.retries)
                runOnIdle {
                    viewModel.state.value =
                        viewModel.state.value.copy(
                            recoveringSession = false,
                            sessionRecoveryError = CommonError.NoConnection().toTextResource(),
                        )
                }
                onNodeWithTag("retry_session").assertIsEnabled()
                onNodeWithText("Login surface").assertDoesNotExist()
                runOnIdle {
                    viewModel.state.value = SessionState(stage = SessionStageUi.SIGNED_OUT)
                }
                onNodeWithText("Login surface").assertIsDisplayed()
                onNodeWithTag("retry_session").assertDoesNotExist()
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
