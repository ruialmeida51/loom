package com.eyther.lumbridge.features.expenses.data.media

/** The native codec normalizes orientation and strips original metadata before upload. */
internal expect suspend fun compressReceiptPhoto(bytes: ByteArray): ByteArray
