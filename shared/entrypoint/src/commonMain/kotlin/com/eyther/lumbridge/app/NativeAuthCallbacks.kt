package com.eyther.lumbridge.app

import com.eyther.lumbridge.features.authentication.data.platform.NativeAuthenticationBridge

fun openAuthenticationCallback(url: String) {
    NativeAuthenticationBridge.open(url = url)
}
