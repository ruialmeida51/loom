package com.eyther.lumbridge.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun ApplySystemTheme(dark: Boolean) {
    SideEffect {
        UIApplication.sharedApplication.keyWindow?.overrideUserInterfaceStyle =
            if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}
