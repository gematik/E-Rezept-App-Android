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

package de.gematik.ti.erp.app.fhir.pharmacy.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.common.model.original.FhirFullUrlResourceEntry.Companion.getFhirVzdResourceType
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDBundle.Companion.getBundle
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDHealthcareService
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDHealthcareService.Companion.getHealthcareService
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDHealthcareService.Companion.getOpeningHours
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDHealthcareService.Companion.getSpecialities
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDLocation
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDLocation.Companion.getLocation
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdAddress.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdEndpoint
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdIdentifier.Companion.getTelematikId
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdIdentifier.Companion.getUid
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdOrganization
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdPosition.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdResourceType
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdTelecom.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.utils.Quad
import de.gematik.ti.erp.app.utils.letNotNullOnCondition
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Requirement(
    "O.Source_2#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser maps and sanitizes structured FHIR pharmacy directory data by:
            • Using `bundleElement.getBundle` to parse only when the structure matches.
            • Using `entry.getResourceType` to match to know resources which are type-safe from Enums as `FhirVzdResourceType`
            • Matching related `HealthcareService`, `Location`, and `Organization` resources via UID-based grouping.
            • Ensuring only records with valid `telematikId` are mapped into ERP models.
            • Skipping malformed or partial resource sets without failing the bundle parsing.
        This satisfies safe mapping of structured directory data into domain entities.
    """
)
class PharmacyBundleParser : BundleParser {

    companion object {
        const val FHIRVZD_TAG = "fhirvzd"
    }

    override fun extract(bundle: JsonElement): FhirPharmacyErpModelCollection {
        return runCatching {
            val bundleElement = bundle.jsonObject
            val fhirVzdBundle = bundleElement.getBundle()
            val numberOfEntries = fhirVzdBundle.numberOfEntries

            val entries = fhirVzdBundle.entries

            Napier.i(tag = FHIRVZD_TAG) { "entries.size $numberOfEntries" }

            // key-value pair (id, quad(health, location, org, endpoint))
            val pharmacyMap = mutableMapOf<String, Quad<FhirVZDHealthcareService?, FhirVZDLocation?, FhirVzdOrganization?, List<FhirVzdEndpoint>?>?>()

            // required only when we activate zuweisung-ohne-telematik-id
            // val endpointsMap = entries.getEndPointsGroupedByTelematikId()

            entries.map { entry ->
                val type = entry.getFhirVzdResourceType()
                val resource = entry.resource
                when (type) {
                    FhirVzdResourceType.Organization -> {
                        val organization = resource?.getOrganization()
                        organization?.identifiers?.getUid()?.let { id ->

                            // though end-point is at base level, we can access it only using telematik-id which is present only in organization
                            // val requiredEndpoints = endpointsMap[organization.telematikId]?.filterByType()

                            pharmacyMap[id] =
                                pharmacyMap[id]
                                    ?.copy(third = organization, fourth = null)
                                    ?: Quad(null, null, organization, null)
                        } ?: logInfo(type)
                    }

                    FhirVzdResourceType.Location -> {
                        val location = resource?.getLocation()
                        location?.identifiers?.getUid()?.let { id ->
                            pharmacyMap[id] = pharmacyMap[id]?.copy(second = location) ?: Quad(null, location, null, null)
                        } ?: logInfo(type)
                    }

                    FhirVzdResourceType.HealthcareService -> {
                        val healthcareService = resource?.getHealthcareService()
                        healthcareService?.identifiers?.getUid()?.let { id ->
                            pharmacyMap[id] = pharmacyMap[id]?.copy(first = healthcareService) ?: Quad(healthcareService, null, null, null)
                        } ?: logInfo(type)
                    }

                    else -> {
                        // not parsed since its an unknown resource type
                    }
                }
            }

            val pharmacyEntries = pharmacyMap.toEntries()

            Napier.i(tag = FHIRVZD_TAG) { "pharmacyEntries.size ${pharmacyEntries.size}" }

            FhirPharmacyErpModelCollection(
                type = PharmacyVzdService.FHIRVZD,
                id = fhirVzdBundle.id,
                total = numberOfEntries,
                entries = pharmacyEntries
            )
        }.orElseEmpty()
    }

    private fun Result<FhirPharmacyErpModelCollection>.orElseEmpty(): FhirPharmacyErpModelCollection =
        getOrElse { FhirPharmacyErpModelCollection.emptyCollection() }

    private fun MutableMap<String,
        Quad<FhirVZDHealthcareService?, FhirVZDLocation?, FhirVzdOrganization?, List<FhirVzdEndpoint>?>?
        >.toEntries(): List<FhirPharmacyErpModel> {
        return entries.mapNotNull { entry ->
            letNotNullOnCondition(
                first = entry.value?.first,
                second = entry.value?.second,
                third = entry.value?.third,
                condition = {
                    entry.value?.third?.identifiers?.getTelematikId() != null // always has a telematik-id to identify the pharmacy
                }
            ) { healthcareService, location, organization ->
                FhirPharmacyErpModel(
                    id = healthcareService.id,
                    name = organization.name,
                    telematikId = organization.telematikId,
                    address = location.address?.toErpModel(),
                    position = location.position?.toErpModel(),
                    contact = healthcareService.telecom.toErpModel(),
                    availableTime = healthcareService.getOpeningHours(),
                    specialities = healthcareService.getSpecialities()
                )
            }
        }
    }

    private fun logInfo(resourceType: FhirVzdResourceType) {
        run {
            Napier.i(tag = FHIRVZD_TAG) { "${resourceType.name} without uid, not parsing it" }
        }
    }
}
