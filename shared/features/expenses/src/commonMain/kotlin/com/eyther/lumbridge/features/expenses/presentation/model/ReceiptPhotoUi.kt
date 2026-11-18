package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.io.bytestring.ByteString

@Immutable data class ReceiptPhotoUi(val id: String, val data: ByteString)
