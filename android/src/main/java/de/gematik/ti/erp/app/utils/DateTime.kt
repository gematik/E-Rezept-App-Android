package de.gematik.ti.erp.app.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val dateTimeShortFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

fun dateTimeShortText(instant: Instant): String = LocalDateTime.ofEpochSecond(
    instant.epochSecond, 0,
    ZoneOffset.UTC
).atOffset(ZoneOffset.UTC).format(dateTimeShortFormatter)
