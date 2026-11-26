package com.eyther.lumbridge.core.domain

import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun newId(): String = Uuid.random().toString()

fun today(timeZone: String = TimeZone.currentSystemDefault().id): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.of(timeZone)).date

fun todayIso(timeZone: String = TimeZone.currentSystemDefault().id): String =
    today(timeZone = timeZone).toString()

fun nowIso(): String = Clock.System.now().toString()
