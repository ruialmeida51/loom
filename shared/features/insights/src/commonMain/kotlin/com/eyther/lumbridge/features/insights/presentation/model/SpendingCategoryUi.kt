package com.eyther.lumbridge.features.insights.presentation.model

data class SpendingCategoryUi(
    val category: String,
    val amountMinor: Long,
    val totalMinor: Long,
) {

    val progress: Float = if (totalMinor > 0) {
        (amountMinor.toDouble() / totalMinor).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
}
