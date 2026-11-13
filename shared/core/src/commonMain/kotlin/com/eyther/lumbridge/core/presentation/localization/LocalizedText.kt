package com.eyther.lumbridge.core.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun genericErrorMessage(): String =
    stringResource(Res.string.ui_something_went_wrong_please_try_again)
