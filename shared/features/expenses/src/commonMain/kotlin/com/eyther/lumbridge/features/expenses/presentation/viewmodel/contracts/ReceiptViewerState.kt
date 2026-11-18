package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi

data class ReceiptViewerState(
    val photo: ReceiptPhotoUi? = null,
    val loading: Boolean = true,
    val error: TextResource? = null,
)
