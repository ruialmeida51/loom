package com.eyther.lumbridge.app.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoPictures
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.picture.LocalPictureLoader
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_loom_preview_sample_data
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_open_preview
import org.jetbrains.compose.resources.stringResource

/** Production LoomApp never selects these fixtures or port implementations. */
@Composable
fun LoomDemoApp() {
    val stores = remember {
        DemoHouseholdStore.households.associate { it.id to DemoHouseholdStore(it) }
    }
    var selectedId by remember { mutableStateOf(DemoHouseholdStore.households.first().id) }
    var started by remember { mutableStateOf(true) }
    val theme by ThemePreferences.mode.collectAsState()
    CompositionLocalProvider(LocalPictureLoader provides DemoPictures) {
        LoomTheme(ThemeMode.valueOf(theme)) {
            Surface {
                Column {
                    Text(
                        stringResource(Res.string.ui_loom_preview_sample_data),
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (started)
                        DemoPortalHost(
                            stores = stores,
                            selectedId = selectedId,
                            onSelect = { selectedId = it },
                            onSignOut = { started = false },
                        )
                    else LoomButton(stringResource(Res.string.ui_open_preview), { started = true })
                }
            }
        }
    }
}
