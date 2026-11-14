package com.eyther.lumbridge.features.authentication.data.network.config

object AuthenticationRedirect {
    const val SCHEME = "com.eyther.lumbridge"
    const val HOST = "auth"
    const val PATH = "/callback"
    const val URL = "$SCHEME://$HOST$PATH"
}
