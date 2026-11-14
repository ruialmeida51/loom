package com.eyther.lumbridge.core.presentation.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.platformCollectAsStateOnStart(): State<T> = collectAsStateWithLifecycle()
