/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.utils

import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.fhir.parser.YearMonth
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalInstant
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalLocalDate
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalLocalDateTime
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalLocalTime
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalYear
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalYearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The Fhir documentation mentions the following formats:
 *
 * instant YYYY-MM-DDThh:mm:ss.sss+zz:zz
 * datetime YYYY, YYYY-MM, YYYY-MM-DD or YYYY-MM-DDThh:mm:ss+zz:zz
 * date YYYY, YYYY-MM, or YYYY-MM-DD
 * time hh:mm:ss
 *
 */

// keep the regex - but we can't be sure about all the different patterns
// val FhirInstantRegex = """(\d\d\d\d-\d\d-\d\d)T(\d\d:\d\d:\d\d)(\.\d\d\d)?(([+-]\d\d:\d\d)|Z)""".toRegex()
// val FhirLocalDateTimeRegex = """(\d\d\d\d-\d\d-\d\d)T(\d\d:\d\d(:\d\d)?)(\.\d\d\d)?""".toRegex()
// val FhirLocalDateRegex = """(\d\d\d\d-\d\d-\d\d)""".toRegex()
val FhirYearMonthRegex = """(?<year>\d\d\d\d)-(?<month>\d\d)""".toRegex()
val FhirYearRegex = """(?<year>\d\d\d\d)""".toRegex()
// val FhirLocalTimeRegex = """(\d\d:\d\d(:\d\d)?)""".toRegex()

/*
  Since kotlinx.serialization does not support PolymorphicSerializer of nullable types
  out of the box we need to add a type to let the serializer know the difference if it is
  a sealed class or sealed interface.
*/
enum class FhirTemporalSerializationType {
    FhirTemporalInstant,
    FhirTemporalLocalDateTime,
    FhirTemporalLocalDate,
    FhirTemporalLocalTime,
    FhirTemporalYearMonth,
    FhirTemporalYear,
}

// todo: move to some other package
@Serializable(with = FhirTemporalSerializer::class)
sealed interface FhirTemporal {
    @Serializable
    @SerialName("Instant")
    data class Instant(
        val value: kotlinx.datetime.Instant,
        val type: FhirTemporalSerializationType = FhirTemporalInstant
    ) : FhirTemporal

    @Serializable
    @SerialName("LocalDateTime")
    data class LocalDateTime(
        val value: kotlinx.datetime.LocalDateTime,
        val type: FhirTemporalSerializationType = FhirTemporalLocalDateTime
    ) : FhirTemporal

    @Serializable
    @SerialName("LocalDate")
    data class LocalDate(
        val value: kotlinx.datetime.LocalDate,
        val type: FhirTemporalSerializationType = FhirTemporalLocalDate
    ) : FhirTemporal

    @Serializable
    @SerialName("YearMonth")
    data class YearMonth(
        val value: de.gematik.ti.erp.app.fhir.parser.YearMonth,
        val type: FhirTemporalSerializationType = FhirTemporalYearMonth
    ) : FhirTemporal

    @Serializable
    @SerialName("Year")
    data class Year(
        val value: de.gematik.ti.erp.app.fhir.parser.Year,
        val type: FhirTemporalSerializationType = FhirTemporalYear
    ) : FhirTemporal

    @Serializable
    @SerialName("LocalTime")
    data class LocalTime(
        val value: kotlinx.datetime.LocalTime,
        val type: FhirTemporalSerializationType = FhirTemporalLocalTime
    ) : FhirTemporal

    fun formattedString(): String =
        when (this) {
            is Instant -> this.value.toString()
            is LocalDate -> this.value.toString()
            is LocalDateTime -> this.value.toString()
            is LocalTime -> this.value.toString()
            is Year -> this.value.toString()
            is YearMonth -> this.value.toString()
        }

    fun toInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): kotlinx.datetime.Instant =
        when (this) {
            is Instant -> this.value
            is LocalDate -> this.value.atStartOfDayIn(timeZone)
            is LocalDateTime -> this.value.toInstant(timeZone)
            is LocalTime, is Year, is YearMonth -> error("invalid format")
        }

    fun toFormattedDate(): String? = this.toInstant(TimeZone.currentSystemDefault())
        .toFormattedDate()
}

object FhirTemporalSerializer : JsonContentPolymorphicSerializer<FhirTemporal>(FhirTemporal::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out FhirTemporal> {
        element.jsonObject["type"]?.jsonPrimitive?.content?.let { classType ->
            return when (FhirTemporalSerializationType.valueOf(classType)) {
                FhirTemporalInstant -> FhirTemporal.Instant.serializer()
                FhirTemporalLocalDateTime -> FhirTemporal.LocalDateTime.serializer()
                FhirTemporalLocalDate -> FhirTemporal.LocalDate.serializer()
                FhirTemporalLocalTime -> FhirTemporal.LocalTime.serializer()
                FhirTemporalYearMonth -> FhirTemporal.YearMonth.serializer()
                FhirTemporalYear -> FhirTemporal.Year.serializer()
            }
        }
            ?: throw SerializationException(
                "FhirTemporalSerializer: key 'type' not found or does not matches any module type"
            )
    }
}

fun Instant.asFhirTemporal(): FhirTemporal.Instant {
    val desiredFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val updatedInstant = Instant.parse(toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime().format(desiredFormatter))
    return FhirTemporal.Instant(updatedInstant)
}

fun Instant.toFormattedDateTime(): String? = this.toLocalDateTime(TimeZone.currentSystemDefault())
    .toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT))

fun Instant.toStartOfDayInUTC(): Instant {
    val currentLocalDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return currentLocalDateTime.date.atStartOfDayIn(TimeZone.UTC)
}

fun Instant.toFormattedDate(): String {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
        .date.toJavaLocalDate().format(dateFormatter)
}

fun LocalDateTime.asFhirTemporal() = FhirTemporal.LocalDateTime(this)
fun LocalDate.asFhirTemporal() = FhirTemporal.LocalDate(this)
fun YearMonth.asFhirTemporal() = FhirTemporal.YearMonth(this)
fun Year.asFhirTemporal() = FhirTemporal.Year(this)
fun LocalTime.asFhirTemporal() = FhirTemporal.LocalTime(this)

@Suppress("ReturnCount")
fun String.toFhirTemporal(): FhirTemporal {
    // going from the most specific to the least

    try {
        return FhirTemporal.Instant(Instant.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalDateTime(LocalDateTime.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalDate(LocalDate.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.YearMonth(YearMonth.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.Year(Year.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalTime(LocalTime.parse(this))
    } catch (_: IllegalArgumentException) {
    }

    error("Couldn't parse `$this`")
}

fun JsonPrimitive.toFhirTemporal() =
    this.contentOrNull?.toFhirTemporal()

fun JsonPrimitive.asFhirLocalTime(): FhirTemporal.LocalTime? =
    this.contentOrNull?.let {
        FhirTemporal.LocalTime(LocalTime.parse(it))
    }

fun JsonPrimitive.asFhirLocalDate(): FhirTemporal.LocalDate? =
    this.contentOrNull?.let {
        FhirTemporal.LocalDate(LocalDate.parse(it))
    }

fun JsonPrimitive.asFhirInstant(): FhirTemporal.Instant? =
    this.contentOrNull?.let {
        FhirTemporal.Instant(Instant.parse(it))
    }

// TODO: find a better place/way for this
fun LocalTime.toHourMinuteString(): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

fun Instant.toLocalDate() = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
fun LocalDate.formattedString(): String {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return this.toJavaLocalDate().format(dateFormatter)
}

fun LocalDate.formattedStringShort(): String {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    return this.toJavaLocalDate().format(dateFormatter)
}

fun LocalDate.isMaxDate() = this == maxLocalDate()
fun LocalDate.isBeforeCurrentDate(currentDate: LocalDate) = this < currentDate
fun LocalDate.isInFuture(currentDate: LocalDate) = this > currentDate
fun LocalDate.atCurrentTime(now: Instant): Instant = this.atTime(now.toLocalDateTime(TimeZone.currentSystemDefault()).time).toInstant(
    TimeZone.currentSystemDefault()
)

fun maxLocalDate() = LocalDate.parse("9999-12-31")
