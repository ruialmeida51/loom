package com.eyther.lumbridge.features.feed.presentation.mapper

import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import kotlin.time.Instant
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
        publishedOn =
            try {
                Instant.parse(publishedAt).toLocalDateTime(TimeZone.of(timeZone)).date
            } catch (_: IllegalArgumentException) {
                null
            },
    )
