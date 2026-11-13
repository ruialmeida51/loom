package com.eyther.lumbridge.core.presentation

import androidx.lifecycle.ViewModel
import com.eyther.lumbridge.core.viewmodel.lifecycle.launch as launchForViewModel
import kotlinx.coroutines.CoroutineScope

abstract class LoomViewModel : ViewModel() {
    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        launchForViewModel(block = block)
}
