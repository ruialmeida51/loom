package com.eyther.lumbridge.app

import androidx.navigation3.runtime.NavKey
import com.eyther.lumbridge.app.navigation.model.HomeDestination
import com.eyther.lumbridge.app.navigation.model.HouseholdDestination
import com.eyther.lumbridge.app.navigation.model.MoreDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class TaskNavigationCompatibilityTest {
    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(HomeDestination.LegacyReminders::class)
                subclass(MoreDestination.LegacyReminders::class)
                subclass(HouseholdDestination.Balances::class)
                subclass(HouseholdDestination.Tasks::class)
            }
        }
    }
    private val serializer = PolymorphicSerializer(NavKey::class)

    @Test
    fun savedReminderRoutesStillDecodeForHostRedirection() {
        assertEquals(
            HomeDestination.LegacyReminders(compose = true),
            json.decodeFromString(
                serializer,
                """{"type":"com.eyther.lumbridge.app.navigation.model.HomeDestination.Reminders","compose":true}""",
            ),
        )
        assertEquals(
            MoreDestination.LegacyReminders,
            json.decodeFromString(
                serializer,
                """{"type":"com.eyther.lumbridge.app.navigation.model.MoreDestination.Reminders"}""",
            ),
        )
        assertEquals(
            HouseholdDestination.Balances,
            json.decodeFromString(
                serializer,
                """{"type":"com.eyther.lumbridge.app.navigation.HouseholdDestination.Money"}""",
            ),
        )
    }
}
