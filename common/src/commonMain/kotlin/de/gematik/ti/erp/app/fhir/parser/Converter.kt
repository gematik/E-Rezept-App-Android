/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.fhir.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * The Fhir documentation mentions the following formats:
 *
 * instant YYYY-MM-DDThh:mm:ss.sss+zz:zz
 * datetime YYYY, YYYY-MM, YYYY-MM-DD or YYYY-MM-DDThh:mm:ss+zz:zz
 * date YYYY, YYYY-MM, or YYYY-MM-DD
 * time hh:mm:ss
 *
 */

private const val DtFormatterPattern = "[HH:mm:ss][yyyy[-MM[-dd]]['T'HH:mm:ss[.SSS]XXX]]"
private const val DtFormatterPatternDateTime = "yyyy[-MM[-dd]]'T'HH:mm:ss[XXX]"
private const val DtFormatterPatternTime = "HH:mm:ss"

private val Formatter = DateTimeFormatter.ofPattern(DtFormatterPattern)
private val FormatterDateTime = DateTimeFormatter.ofPattern(DtFormatterPatternDateTime)
private val FormatterTime = DateTimeFormatter.ofPattern(DtFormatterPatternTime)

fun String?.asTemporalAccessor(): TemporalAccessor? =
    this?.let {
        Formatter
            .parseBest(
                it,
                Instant::from,
                LocalDate::from,
                YearMonth::from,
                Year::from,
                LocalTime::from
            )
    }

fun TemporalAccessor?.asFormattedString(): String? =
    when (this) {
        is Instant -> this.toString()
        is LocalDateTime -> FormatterDateTime.withZone(ZoneOffset.UTC).format(this)
        is TemporalAccessor -> Formatter.withZone(ZoneOffset.UTC).format(this)
        else -> null
    }

fun JsonPrimitive.asTemporalAccessor(): TemporalAccessor? =
    this.contentOrNull?.let {
        Formatter
            .parseBest(
                it,
                Instant::from,
                LocalDate::from,
                YearMonth::from,
                Year::from,
                LocalTime::from
            )
    }

fun JsonPrimitive.asLocalTime(): LocalTime? =
    this.contentOrNull?.let {
        FormatterTime.parse(it, LocalTime::from)
    }

fun JsonPrimitive.asLocalDateTime(): LocalDateTime? =
    this.contentOrNull?.let {
        Formatter.parse(it, LocalDateTime::from)
    }

fun JsonPrimitive.asLocalDate(): LocalDate? =
    this.contentOrNull?.let {
        Formatter.parse(it, LocalDate::from)
    }

fun JsonPrimitive.asInstant(): Instant? =
    this.contentOrNull?.let {
        Formatter.parse(it, Instant::from)
    }

/**
 * Returns the first element in the JSON structure. For arrays this is the first element.
 *
 * With [this] being the element of `foo`,
 * `{ "foo": "bar" }` and `{ "foo": [ "bar" ] }`
 * return both the [JsonPrimitive] with its content `bar`.
 */
fun JsonElement.contained() =
    when (this) {
        is JsonArray -> this.first()
        else -> this
    }

fun JsonElement.containedOrNull() =
    when (this) {
        is JsonArray -> this.firstOrNull()
        else -> this
    }

fun JsonElement.containedObject() =
    this.contained().jsonObject

fun JsonElement.containedObjectOrNull() =
    this.containedOrNull() as? JsonObject

/**
 * Returns the first contained array or otherwise [this] if the contained type is not an array.
 * If [this] is not an array as well, `null` is returned.
 *
 * With [this] being the element of `foo`,
 * `{ "foo": [ [ { "bar": "baz" } ] ] }` and `{ "foo": [ { "bar": "baz" } ] }`
 * return both the [JsonArray] with its content `[ { "bar": "baz" } ]`.
 */
fun JsonElement.containedArray() =
    this.contained() as? JsonArray ?: this.jsonArray

fun JsonElement.containedArrayOrNull() =
    this.containedOrNull() as? JsonArray ?: this as? JsonArray

fun JsonElement.containedString() =
    this.contained().jsonPrimitive.content

fun JsonElement.containedStringOrNull() =
    (this.containedOrNull() as? JsonPrimitive)?.contentOrNull

fun JsonElement.containedBoolean() =
    this.contained().jsonPrimitive.boolean

fun JsonElement.containedBooleanOrNull() =
    (this.containedOrNull() as? JsonPrimitive)?.booleanOrNull

fun JsonElement.containedInt() =
    this.contained().jsonPrimitive.int

fun JsonElement.containedIntOrNull() =
    (this.containedOrNull() as? JsonPrimitive)?.intOrNull

fun JsonElement.containedDouble() =
    this.contained().jsonPrimitive.double

fun JsonElement.containedDoubleOrNull() =
    (this.containedOrNull() as? JsonPrimitive)?.doubleOrNull

/**
 * Will return the first element in the JSON structure.
 *
 * With [this] being the element of `foo` and [key] is `bar`,
 * `{ "foo": { "bar": "baz" } }` and `{ "foo": [ { "bar": "baz" } ] }`
 * return both the [JsonPrimitive] with its content `baz`.
 */
fun JsonElement.contained(key: String) =
    when (this) {
        is JsonObject -> this[key] ?: error("`$key` not found")
        is JsonArray -> this.first().jsonObject[key] ?: error("`$key` not found")
        else -> error("`this` needs to be JsonObject or JsonArray")
    }

fun JsonElement.containedOrNull(key: String) =
    when (this) {
        is JsonObject -> this[key]
        is JsonArray -> (this.firstOrNull() as? JsonObject)?.get(key)
        else -> null
    }

fun JsonElement.containedObject(key: String) =
    this.contained(key).containedObject()

fun JsonElement.containedArray(key: String) =
    this.contained(key).containedArray()

fun JsonElement.containedArrayOrNull(key: String) =
    this.containedOrNull(key)?.containedArrayOrNull()

fun JsonElement.containedString(key: String) =
    this.contained(key).containedString()

fun JsonElement.containedStringOrNull(key: String) =
    this.containedOrNull(key)?.containedStringOrNull()

fun JsonElement.containedBoolean(key: String) =
    this.contained(key).containedBoolean()

fun JsonElement.containedBooleanOrNull(key: String) =
    this.containedOrNull(key)?.containedBooleanOrNull()

fun JsonElement.containedInt(key: String) =
    this.contained(key).containedInt()

fun JsonElement.containedIntOrNull(key: String) =
    this.containedOrNull(key)?.containedIntOrNull()

fun JsonElement.containedDouble(key: String) =
    this.contained(key).containedDouble()

fun JsonElement.containedDoubleOrNull(key: String) =
    this.containedOrNull(key)?.containedDoubleOrNull()
