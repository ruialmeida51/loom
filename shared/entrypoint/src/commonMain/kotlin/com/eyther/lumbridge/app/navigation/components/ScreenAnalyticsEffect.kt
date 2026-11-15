package com.eyther.lumbridge.app.navigation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import org.koin.compose.koinInject

/** Navigation supplies only a fixed screen name; personal and financial data stay out of events. */
@Composable
internal fun ScreenAnalyticsEffect(screen: AnalyticsScreenDomain?) {
    val preferences: GetAnalyticsPreferencesStreamUseCase = koinInject()
    val trackEvent: TrackAnalyticsEventUseCase = koinInject()
    val consent by preferences().collectAsState()
    LaunchedEffect(screen, consent.enabled) {
        if (screen != null && consent.enabled) {
            trackEvent(event = AnalyticsEventDomain.ScreenViewed(screen = screen))
        }
    }
}
