package com.eyther.lumbridge.features.feed.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toFeedError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code == "23505" && error.contains("\"feed_subscriptions_household_id_url_key\"")) {
        return FeedError.AlreadyFollowed(cause = this)
    }

    if (code != "P0001") return toAppError()

    return when (error) {

        else -> toAppError()
    }
}
