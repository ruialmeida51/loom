package com.eyther.lumbridge.app.navigation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class HouseholdDestinationTest {

    @Test
    fun savedNavigationKeepsItsFormatAfterMovingRoutePackages() {
        val saved = """{"type":"com.eyther.lumbridge.app.navigation.HouseholdDestination.Home"}"""

        assertEquals(HouseholdDestination.Home, Json.decodeFromString<HouseholdDestination>(saved))
        val encoded = Json.encodeToString<HouseholdDestination>(HouseholdDestination.Home)
        assertEquals(Json.decodeFromString<JsonObject>(saved), Json.decodeFromString<JsonObject>(encoded))
    }
}
