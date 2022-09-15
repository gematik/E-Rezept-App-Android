/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.pharmacy.repository.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.EmergencyPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.LocalPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningHours
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningTime
import de.gematik.ti.erp.app.pharmacy.repository.model.Pharmacy
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyAddress
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacySearchResult
import de.gematik.ti.erp.app.pharmacy.repository.model.RoleCode
import de.gematik.ti.erp.app.prescription.repository.extractResources
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.HealthcareService
import org.hl7.fhir.r4.model.Location.LocationStatus
import java.time.DayOfWeek
import java.time.LocalTime
import org.hl7.fhir.r4.model.Location as FhirLocation

typealias FhirLocationHoursOfOperationComponent = FhirLocation.LocationHoursOfOperationComponent
typealias FhirHealthcareServiceAvailableTimeComponent = HealthcareService.HealthcareServiceAvailableTimeComponent

private const val OUT_PHARMACY = "OUTPHARM"
private const val MOBL = "MOBL"

object PharmacyMapper {
    /**
     * Extract pharmacy services from a search bundle.
     */
    fun extractLocalPharmacyServices(bundle: Bundle): PharmacySearchResult {
        val locations = bundle.extractResources<FhirLocation>()

        return PharmacySearchResult(
            pharmacies = locations?.mapNotNull { location ->
                runCatching {
                    val locationName = requireNotNull(location.name)
                    val localService = listOf(
                        LocalPharmacyService(
                            name = locationName,
                            openingHours = location.hoursOfOperation.mapToOpeningHours()
                        )
                    )

                    val otherServices = location.contained?.mapNotNull { resource ->
                        (resource as? HealthcareService)?.let { healthCareService ->
                            when (healthCareService.typeFirstRep?.codingFirstRep?.code) {
                                "498" -> {
                                    DeliveryPharmacyService(
                                        name = locationName,
                                        openingHours = healthCareService.availableTime.mapToOpeningHours()
                                    )
                                }
                                "117" -> {
                                    EmergencyPharmacyService(
                                        name = locationName,
                                        openingHours = healthCareService.availableTime.mapToOpeningHours()
                                    )
                                }
                                else -> null
                            }
                        }
                    } ?: emptyList()

                    Pharmacy(
                        name = locationName,
                        location = location.position.mapToLocation(),
                        address = location.address?.let { address ->
                            PharmacyAddress(
                                lines = address.line.mapNotNull { it.value },
                                postalCode = address.postalCode ?: "",
                                city = address.city ?: ""
                            )
                        } ?: PharmacyAddress(listOf(), "", ""),
                        contacts = location.telecom.mapToContacts(),
                        provides = localService + otherServices,
                        telematikId = requireNotNull(location.identifier?.find { it.system == "https://gematik.de/fhir/NamingSystem/TelematikID" }?.value),
                        roleCode = roleCodes(location.type),
                        ready = location.status == LocationStatus.ACTIVE
                    )
                }.getOrNull()
            } ?: emptyList(),
            bundleId = bundle.id,
            bundleResultCount = bundle.total,
        )
    }

    private fun roleCodes(coding: MutableList<CodeableConcept>): List<RoleCode> {
        return coding.map {
            when (it.coding[0].code) {
                OUT_PHARMACY -> RoleCode.OUT_PHARM
                MOBL -> RoleCode.MOBL
                else -> RoleCode.PHARM
            }
        }
    }

    private fun FhirLocation.LocationPositionComponent.mapToLocation(): Location =
        Location(
            latitude = this.latitude.toDouble(),
            longitude = this.longitude.toDouble(),
        )

    private fun List<ContactPoint>.mapToContacts(): PharmacyContacts =
        PharmacyContacts(
            phone = this.find { it.system == ContactPoint.ContactPointSystem.PHONE }?.value ?: "",
            mail = this.find { it.system == ContactPoint.ContactPointSystem.EMAIL }?.value ?: "",
            url = this.find { it.system == ContactPoint.ContactPointSystem.URL }?.value ?: ""
        )

    @JvmName("mapToOpeningHoursForLocationTime")
    private fun List<FhirLocationHoursOfOperationComponent>.mapToOpeningHours() =
        mapNotNull { fhirHours ->
            fhirHours.daysOfWeek.mapNotNull { mapFhirDayOfWeekToDayOfWeek(it?.valueAsString) }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    mapOpeningHours(
                        days = it,
                        openingTime = runCatching { LocalTime.parse(fhirHours.openingTime) }.getOrDefault(
                            LocalTime.MIN
                        ),
                        closingTime = runCatching { LocalTime.parse(fhirHours.closingTime) }.getOrDefault(
                            LocalTime.MAX
                        ),
                    )
                }
        }.flatten().fold(mutableMapOf<DayOfWeek, List<OpeningTime>>()) { acc, v ->
            acc[v.first] = acc.getOrDefault(v.first, emptyList()) + v.second
            acc
        }.let {
            OpeningHours(it)
        }

    @JvmName("mapToOpeningHoursForHealthcareServiceTime")
    private fun List<FhirHealthcareServiceAvailableTimeComponent>.mapToOpeningHours() =
        mapNotNull { fhirHours ->
            fhirHours.daysOfWeek.mapNotNull { mapFhirDayOfWeekToDayOfWeek(it?.valueAsString) }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    mapOpeningHours(
                        days = it,
                        openingTime = runCatching { LocalTime.parse(fhirHours.availableStartTime) }.getOrDefault(
                            LocalTime.MIN
                        ),
                        closingTime = runCatching { LocalTime.parse(fhirHours.availableEndTime) }.getOrDefault(
                            LocalTime.MAX
                        ),
                    )
                }
        }.flatten().fold(mutableMapOf<DayOfWeek, List<OpeningTime>>()) { acc, v ->
            acc[v.first] = acc.getOrDefault(v.first, emptyList()) + v.second
            acc
        }.let {
            OpeningHours(it)
        }

    private fun mapFhirDayOfWeekToDayOfWeek(day: String?) =
        when (day) {
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            "sun" -> DayOfWeek.SUNDAY
            else -> null
        }

    private fun mapOpeningHours(days: List<DayOfWeek>, openingTime: LocalTime, closingTime: LocalTime) =
        days.map {
            Pair(it, listOf(OpeningTime(openingTime = openingTime, closingTime = closingTime)))
        }
}
