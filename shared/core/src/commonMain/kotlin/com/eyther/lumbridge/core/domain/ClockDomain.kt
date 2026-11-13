package com.eyther.lumbridge.core.domain

import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun newId(): String = Uuid.random().toString()

fun todayIso(timeZone: String = TimeZone.currentSystemDefault().id): String =
    Clock.System.now().toLocalDateTime(TimeZone.of(timeZone)).date.toString()

fun nowIso(): String = Clock.System.now().toString()
