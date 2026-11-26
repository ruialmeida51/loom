package com.eyther.lumbridge.features.feed.presentation.mapper

import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun ArticleDomain.toPreviewUi(timeZone: String): ArticlePreviewUi =
    ArticlePreviewUi(
        id = id,
        title = title,
        url = url,
        source = source,
        summary = summary,
        read = read,
        imageUrl = imageUrl,
        publishedOn = publishedAt.toLocalDateTime(TimeZone.of(timeZone)).date,
    )
