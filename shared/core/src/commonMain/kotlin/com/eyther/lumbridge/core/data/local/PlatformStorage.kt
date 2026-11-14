package com.eyther.lumbridge.core.data.local

/** Native encrypted storage for sessions and private app preferences. */
expect object PlatformStorage {
    fun get(key: String): String?

    fun put(key: String, value: String?)
}
