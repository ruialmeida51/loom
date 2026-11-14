package com.eyther.lumbridge.core.presentation.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Stable
sealed interface TextResource {

    @Composable
    fun getString(): String

    @Stable
    data class Resource(
        val res: StringResource,
        val args: List<Any> = emptyList(),
    ) : TextResource {

        @Composable
        override fun getString(): String = stringResource(res, *args.toTypedArray())
    }
}
