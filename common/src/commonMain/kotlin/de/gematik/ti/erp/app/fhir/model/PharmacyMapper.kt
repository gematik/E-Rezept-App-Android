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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedInt
import de.gematik.ti.erp.app.fhir.parser.containedObject
import de.gematik.ti.erp.app.fhir.parser.containedObjectOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.not
import de.gematik.ti.erp.app.fhir.parser.or
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPositionErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.MalformedURLException
import java.net.URL

val Contained = listOf("contained")
val TypeCodingCode = listOf("type", "coding", "code")

@Requirement(
    "O.Source_2#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Sanitization is also done for all FHIR mapping."
)
@Deprecated("Note: Will be deleted when apo-vzd is not used anymore")
@Suppress("CyclomaticComplexMethod")
fun extractPharmacyServices(
    bundle: JsonElement,
    onError: (JsonElement, Exception) -> Unit = { _, _ -> }
): FhirPharmacyErpModelCollection {
    val bundleId = bundle.containedString("id")
    val bundleTotal = bundle.containedInt("total")
    val resources = bundle.findAll(listOf("entry", "resource")).filterWith("name", not(stringValue("-")))

    val pharmacies = resources.mapCatching(onError) { pharmacy ->
        val locationId = pharmacy.containedString("id")
        val locationName = pharmacy.containedString("name")
        val specialities = mutableListOf<FhirVzdSpecialtyType>()

        // opening hours from hours of operation
        val hoursOfOperation = pharmacy.containedArrayOrNull("hoursOfOperation")?.let { hoursOfOperation(it) }

        // opening hours from available time
        var openingHours: OpeningHoursErpModel? = null
        // opening hours override from service
        pharmacy
            .findAll(Contained)
            .filterWith(TypeCodingCode, stringValue("498"))
            .firstOrNull()
            ?.let { service ->
                // Add delivery service
                specialities.add(FhirVzdSpecialtyType.Delivery)
                openingHours = service.containedArrayOrNull("availableTime")?.let { availableTime(it) }
                    ?: OpeningHoursErpModel(emptyMap())
            }

        val telematikId =
            pharmacy
                .findAll(listOf("identifier"))
                .filterWith(
                    listOf("system"),
                    or(
                        stringValue("https://gematik.de/fhir/NamingSystem/TelematikID"),
                        stringValue("https://gematik.de/fhir/sid/telematik-id")
                    )
                )
                .first()
                .containedString("value")

        pharmacy.findAll(TypeCodingCode).forEach {
            when (it.containedString()) {
                "MOBL" -> {
                    specialities.add(FhirVzdSpecialtyType.Shipment)
                }

                "OUTPHARM" -> {
                    specialities.add(FhirVzdSpecialtyType.Pickup)
                }
            }
        }

        FhirPharmacyErpModel(
            id = locationId,
            name = locationName,
            telematikId = telematikId,
            position = pharmacy.containedObjectOrNull("position")?.let {
                FhirPositionErpModel(
                    latitude = it.containedDouble("latitude"),
                    longitude = it.containedDouble("longitude")
                )
            },
            address = pharmacy.containedObject("address").let { address ->
                FhirPharmacyAddressErpModel(
                    lineAddress = address.containedArray("line").map { it.containedString() }.joinToString { "" },
                    postalCode = if (BuildKonfig.INTERNAL) {
                        address.containedStringOrNull("postalCode") ?: ""
                    } else {
                        address.containedString("postalCode")
                    },
                    city = address.containedString("city")
                )
            },
            contact = pharmacy.containedArrayOrNull("telecom")?.let { contacts(it) } ?: emptyFhirContactInformationErpModel(),
            hoursOfOperation = hoursOfOperation,
            availableTime = openingHours ?: OpeningHoursErpModel(emptyMap()),
            specialities = specialities.toList()
        )
    }

    return FhirPharmacyErpModelCollection(
        type = PharmacyVzdService.APOVZD,
        entries = pharmacies.toList(),
        id = bundleId,
        total = bundleTotal
    )
}

private fun emptyFhirContactInformationErpModel() =
    FhirContactInformationErpModel("", "", "")

/**
 * Extract certificates from binary bundle.
 */
fun extractBinaryCertificatesAsBase64(
    bundle: JsonElement
): List<String> {
    val resources = bundle.findAll(listOf("entry", "resource"))
    val resourceStrings = mutableListOf<String>()
    for (resource in resources) {
        require(resource.containedString("contentType") == "application/pkix-cert")
        resourceStrings.add(resource.containedString("data"))
    }
    return resourceStrings.toList()
}

fun <R : Any> Sequence<JsonElement>.mapCatching(
    onError: (JsonElement, Exception) -> Unit,
    transform: (JsonElement) -> R?
): Sequence<R> =
    mapNotNull {
        try {
            transform(it)
        } catch (e: Exception) {
            onError(it, e)
            null
        }
    }

private fun sanitizeUrl(url: String): String =
    try {
        require(url.startsWith("http"))

        URL(url).toString()
    } catch (_: MalformedURLException) {
        ""
    } catch (_: IllegalArgumentException) {
        ""
    }

@Suppress("CyclomaticComplexMethod")
private fun contacts(
    telecom: JsonArray
): FhirContactInformationErpModel {
    var phone = ""
    var mail = ""
    var url = ""

    telecom
        .forEach {
            when (it.containedString("system")) {
                "phone" -> phone = it.containedStringOrNull("value") ?: ""
                "email" -> mail = it.containedStringOrNull("value") ?: ""
                "url" -> url = sanitizeUrl(it.containedStringOrNull("value") ?: "")
            }
        }

    return FhirContactInformationErpModel(
        phone = phone,
        mail = mail,
        url = url
    )
}

private fun availableTime(
    hoursOfOperation: JsonArray
): OpeningHoursErpModel =
    openingHours(
        hoursOfOperation = hoursOfOperation,
        startTimeAlias = "availableStartTime",
        endTimeAlias = "availableEndTime"
    )

private fun hoursOfOperation(
    hoursOfOperation: JsonArray
): OpeningHoursErpModel =
    openingHours(
        hoursOfOperation = hoursOfOperation,
        startTimeAlias = "openingTime",
        endTimeAlias = "closingTime"
    )

private fun openingHours(
    hoursOfOperation: JsonArray,
    startTimeAlias: String,
    endTimeAlias: String
): OpeningHoursErpModel =
    hoursOfOperation
        .asSequence()
        .flatMap { fhirHours ->
            (fhirHours as JsonObject).let {
                val openingTime = lookupTime(fhirHours[startTimeAlias]?.jsonPrimitive)?.value
                val closingTime = lookupTime(fhirHours[endTimeAlias]?.jsonPrimitive)?.value

                val time = OpeningTimeErpModel(openingTime = openingTime, closingTime = closingTime)

                fhirHours.containedArray("daysOfWeek")
                    .asSequence()
                    .map { fhirDay(it.containedString()) to time }
            }
        }
        .groupBy({ (day, _) -> day }, { (_, time) -> time })
        .let {
            OpeningHoursErpModel(it)
        }

private fun fhirDay(day: String) =
    when (day) {
        "mon" -> DayOfWeek.MONDAY
        "tue" -> DayOfWeek.TUESDAY
        "wed" -> DayOfWeek.WEDNESDAY
        "thu" -> DayOfWeek.THURSDAY
        "fri" -> DayOfWeek.FRIDAY
        "sat" -> DayOfWeek.SATURDAY
        "sun" -> DayOfWeek.SUNDAY
        else -> error("wrong day format: $day")
    }
