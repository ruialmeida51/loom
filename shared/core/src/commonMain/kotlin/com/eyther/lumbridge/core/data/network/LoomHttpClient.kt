package com.eyther.lumbridge.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout

private const val REQUEST_TIMEOUT_MILLIS = 15_000L

/**
 * The one HTTP client for public web requests (exchange rates, story pictures), provided once by
 * DI so its connection pool and engine threads are shared. Supabase keeps its own client.
 */
fun createLoomHttpClient(): HttpClient =
    HttpClient { install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS } }
