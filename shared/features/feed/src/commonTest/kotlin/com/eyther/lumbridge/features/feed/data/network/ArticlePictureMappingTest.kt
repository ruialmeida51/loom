package com.eyther.lumbridge.features.feed.data.network

import com.eyther.lumbridge.features.feed.data.network.mapper.toDomain
import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.presentation.mapper.toPreviewUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class ArticlePictureMappingTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun article(picture: String?): String =
        """{"id":"a","subscription_id":"s","title":"T","summary":"","url":"https://example.test/a",
           "source":"BBC","published_at":"2026-11-26T12:00:00Z","is_read":false
           ${picture?.let { ""","image_url":"$it"""" } ?: ""}}"""

    @Test
    fun aStoryKeepsItsPictureThroughToTheCard() {
        val domain =
            json.decodeFromString<ArticleResponse>(article("https://img.example.test/a.jpg")).toDomain()
        assertEquals("https://img.example.test/a.jpg", domain.imageUrl)
        assertEquals(
            "https://img.example.test/a.jpg",
            domain.toPreviewUi(timeZone = "Europe/Lisbon").imageUrl,
        )
    }

    @Test
    fun storiesWithoutAPictureOrFromAnOlderServerHaveNone() {
        assertNull(json.decodeFromString<ArticleResponse>(article(null)).toDomain().imageUrl)
        assertNull(
            json.decodeFromString<ArticleResponse>(article("http://img.example.test/a.jpg"))
                .toDomain()
                .imageUrl
        )
    }
}
