package com.eyther.lumbridge.features.home.presentation.model

/** The part of the day Home greets the user with, in the household's time zone. */
enum class HomeGreetingUi {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        fun forHour(hour: Int): HomeGreetingUi =
            when (hour) {
                in 5..11 -> MORNING
                in 12..17 -> AFTERNOON
                else -> EVENING
            }
    }
}
