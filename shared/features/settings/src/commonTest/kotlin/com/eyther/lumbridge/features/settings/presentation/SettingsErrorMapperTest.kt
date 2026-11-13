package com.eyther.lumbridge.features.settings.presentation

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.error_permission_denied
import com.eyther.lumbridge.features.settings.presentation.mapper.toSettingsTextResource
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsErrorMapperTest {

    @Test
    fun notificationPermissionUsesItsTypedErrorResource() {
        assertEquals(
            TextResource.Resource(Res.string.error_permission_denied),
            SettingsError.PermissionDenied().toSettingsTextResource(),
        )
    }

    @Test
    fun exceptionTextCannotSelectAFeatureMessage() {
        val error = CommonError.Unknown(
            IllegalStateException("settings.message_choose_a_theme"),
        )

        assertEquals(
            error.toTextResource(),
            error.toSettingsTextResource(),
        )
    }
}
