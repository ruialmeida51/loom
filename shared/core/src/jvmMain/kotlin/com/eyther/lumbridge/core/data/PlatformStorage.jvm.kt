package com.eyther.lumbridge.core.data

/** Desktop is a development preview. Tokens are intentionally session-only here. */
actual object PlatformStorage {
    private val values = mutableMapOf<String, String>()

    actual fun get(key: String): String? = values[key]

    actual fun put(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}
