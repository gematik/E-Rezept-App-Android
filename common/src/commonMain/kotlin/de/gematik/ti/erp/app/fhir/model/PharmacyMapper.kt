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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.parser.containedArray
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedInt
import de.gematik.ti.erp.app.fhir.parser.containedIntOrNull
import de.gematik.ti.erp.app.fhir.parser.containedObject
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.not
import de.gematik.ti.erp.app.fhir.parser.or
import de.gematik.ti.erp.app.fhir.parser.stringValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement
import java.net.MalformedURLException
import java.net.URL
import kotlinx.datetime.DayOfWeek

val Contained = listOf("contained")
val TypeCodingCode = listOf("type", "coding", "code")

const val PickUpRank = 100
const val DeliveryRank = 200
const val OnlineServiceRank = 300

/**
 * Extract pharmacy services from a search bundle.
 */
@Requirement(
    "A_19984#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Validate incoming Pharmacy data."
)
fun extractPharmacyServices(
    bundle: JsonElement,
    onError: (JsonElement, Exception) -> Unit = { _, _ -> }
): PharmacyServices {
    val bundleId = bundle.containedString("id")
    val bundleTotal = bundle.containedInt("total")
    val resources = bundle.findAll(listOf("entry", "resource")).filterWith("name", not(stringValue("-")))

    val pharmacies = resources.mapCatching(onError) { pharmacy ->
        val locationId = pharmacy.containedString("id")
        val locationName = pharmacy.containedString("name")
        val localService = LocalPharmacyService(
            name = locationName,
            openingHours = pharmacy.containedArrayOrNull("hoursOfOperation")?.let { hoursOfOperation(it) }
                ?: OpeningHours(emptyMap())
        )

        val deliveryPharmacyService =
            pharmacy
                .findAll(Contained)
                .filterWith(TypeCodingCode, stringValue("498"))
                .firstOrNull()
                ?.let { service ->
                    DeliveryPharmacyService(
                        name = locationName,
                        openingHours = service.containedArrayOrNull("availableTime")?.let { availableTime(it) }
                            ?: OpeningHours(emptyMap())
                    )
                }

        // keep it; was initially part of the spec but no pharmacy can provide any emergency service
        //
        // val emergencyPharmacyService = resource
        //    .findAll("contained")
        //    .filterWith("type.coding.code", stringValue("117"))
        //    .firstOrNull()
        //    ?.let {
        //        EmergencyPharmacyService(
        //            name = locationName,
        //            openingHours = availableTime(it.containedArray("availableTime")!!)
        //        )
        //    }

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

        var isOutpatientPharmacy = false
        var isMobilePharmacy = false

        pharmacy.findAll(TypeCodingCode).forEach {
            when (it.containedString()) {
                "OUTPHARM" -> isOutpatientPharmacy = true
                "MOBL" -> isMobilePharmacy = true
            }
        }

        val pickUpPharmacyService = if (isOutpatientPharmacy) {
            PickUpPharmacyService(name = locationName)
        } else {
            null
        }

        val onlinePharmacyService = if (isMobilePharmacy) {
            OnlinePharmacyService(name = locationName)
        } else {
            null
        }

        val position = pharmacy.containedObject("position").let {
            Location(
                latitude = it.containedDouble("latitude"),
                longitude = it.containedDouble("longitude")
            )
        }

        Pharmacy(
            id = locationId,
            name = locationName,
            location = position,
            address = pharmacy.containedObject("address").let { address ->
                PharmacyAddress(
                    lines = address.containedArray("line").map { it.containedString() },
                    postalCode = if (BuildKonfig.INTERNAL) {
                        address.containedStringOrNull("postalCode") ?: ""
                    } else {
                        address.containedString("postalCode")
                    },
                    city = address.containedString("city")
                )
            },
            contacts = pharmacy.containedArrayOrNull("telecom")?.let { contacts(it) } ?: PharmacyContacts(
                "",
                "",
                "",
                "",
                "",
                ""
            ),
            provides = listOfNotNull(
                localService,
                deliveryPharmacyService,
                onlinePharmacyService,
                pickUpPharmacyService
            ),
            telematikId = telematikId,
            ready = pharmacy.containedString("status") == "active"
        )
    }

    return PharmacyServices(
        pharmacies = pharmacies.toList(),
        bundleId = bundleId,
        bundleResultCount = bundleTotal
    )
}

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

private fun <R : Any> Sequence<JsonElement>.mapCatching(
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

private fun contacts(
    telecom: JsonArray
): PharmacyContacts {
    var phone = ""
    var mail = ""
    var url = ""
    var pickUpUrl = ""
    var deliveryUrl = ""
    var onlineServiceUrl = ""

    telecom
        .forEach {
            when (it.containedString("system")) {
                "phone" -> phone = it.containedStringOrNull("value") ?: ""
                "email" -> mail = it.containedStringOrNull("value") ?: ""
                "url" -> url = sanitizeUrl(it.containedStringOrNull("value") ?: "")
                "other" -> when (it.containedIntOrNull("rank")) {
                    PickUpRank -> pickUpUrl = sanitizeUrl(it.containedStringOrNull("value") ?: "")
                    DeliveryRank -> deliveryUrl = sanitizeUrl(it.containedStringOrNull("value") ?: "")
                    OnlineServiceRank -> onlineServiceUrl = sanitizeUrl(it.containedStringOrNull("value") ?: "")
                }
            }
        }

    return PharmacyContacts(
        phone = phone,
        mail = mail,
        url = url,
        pickUpUrl = pickUpUrl,
        deliveryUrl = deliveryUrl,
        onlineServiceUrl = onlineServiceUrl
    )
}

private fun availableTime(
    hoursOfOperation: JsonArray
): OpeningHours =
    openingHours(
        hoursOfOperation = hoursOfOperation,
        startTimeAlias = "availableStartTime",
        endTimeAlias = "availableEndTime"
    )

private fun hoursOfOperation(
    hoursOfOperation: JsonArray
): OpeningHours =
    openingHours(
        hoursOfOperation = hoursOfOperation,
        startTimeAlias = "openingTime",
        endTimeAlias = "closingTime"
    )

private fun openingHours(
    hoursOfOperation: JsonArray,
    startTimeAlias: String,
    endTimeAlias: String
): OpeningHours =
    hoursOfOperation
        .asSequence()
        .flatMap { fhirHours ->
            (fhirHours as JsonObject).let {
                val openingTime = lookupTime(fhirHours[startTimeAlias]?.jsonPrimitive)?.value
                val closingTime = lookupTime(fhirHours[endTimeAlias]?.jsonPrimitive)?.value

                val time = OpeningTime(openingTime = openingTime, closingTime = closingTime)

                fhirHours.containedArray("daysOfWeek")
                    .asSequence()
                    .map { fhirDay(it.containedString()) to time }
            }
        }
        .groupBy({ (day, _) -> day }, { (_, time) -> time })
        .let {
            OpeningHours(it)
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
