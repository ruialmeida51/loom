package com.eyther.lumbridge.features.feed.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.data.selectHouseholdRows
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
private data class ArticleDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String,
    @SerialName("url") val url: String,
    @SerialName("source") val source: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("is_read") val read: Boolean,
) {
    fun toDomain() = ArticleDomain(id, title, summary, url, source, publishedAt, read)
}

@Serializable
private data class FeedDto(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("url") val url: String,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
private data class ReadDto(
    @SerialName("article_id") val articleId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("is_read") val read: Boolean,
)

class CommonFeedDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : FeedDataSource {
    private val sample =
        MutableStateFlow(
            listOf(
                ArticleDomain(
                    newId(),
                    "A slower Sunday starts at home",
                    "A few small rituals for a weekend that feels like your own.",
                    "https://www.theguardian.com/lifeandstyle",
                    "The weekend edit",
                    nowIso(),
                    false,
                ),
                ArticleDomain(
                    newId(),
                    "Good food, less waste",
                    "Simple ideas for making the most of what’s already in your fridge.",
                    "https://www.bbcgoodfood.com/",
                    "Around the table",
                    nowIso(),
                    false,
                ),
                ArticleDomain(
                    newId(),
                    "Make room for a little green",
                    "Easy plants and everyday care for the spaces you share.",
                    "https://www.rhs.org.uk/",
                    "Little living things",
                    nowIso(),
                    true,
                ),
            ),
        )

    override fun observe(): Flow<List<ArticleDomain>> =
        if (context.demo) sample
        else
            access.watch("feed_articles", context.id) {
                access.selectHouseholdRows<ArticleDto>("feed_articles_with_state", context.id).map {
                    it.toDomain()
                }
            }

    override suspend fun follow(url: String) = safeApiCall {
        require(!context.demo) {
            "Follow feeds in your own household. These are sample reading suggestions."
        }
        val id = newId()
        access
            .requireClient()
            .from("feed_subscriptions")
            .insert(FeedDto(id, context.id, url, context.userId))
        access
            .requireClient()
            .functions
            .invoke("refresh-feed", body = buildJsonObject { put("subscription_id", id) })
        Unit
    }

    override suspend fun markRead(id: String, read: Boolean) = safeApiCall {
        if (context.demo)
            sample.update { items -> items.map { if (it.id == id) it.copy(read = read) else it } }
        else access.requireClient().from("article_reads").upsert(ReadDto(id, context.userId, read))
        Unit
    }
}
