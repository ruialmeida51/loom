package com.eyther.lumbridge.features.feed.presentation.model

import kotlinx.datetime.LocalDate

data class ArticlePreviewUi(
    val id: String,
    val title: String,
    val url: String,
    val source: String,
    val publishedOn: LocalDate?,
)
