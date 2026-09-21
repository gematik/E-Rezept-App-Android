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

package de.gematik.ti.erp.app.pharmacy.usecase.mapper

import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.LocationModeErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyOpeningHoursErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyOpeningTimeErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel.DeliveryPharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel.LocalPharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel.OnlinePharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel.PickUpPharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PositionErpModel

fun List<FhirPharmacyErpModel>.toModel(
    locationMode: LocationModeErpModel? = null,
    type: PharmacyVzdService = FHIRVZD
): List<PharmacyDetailsErpModel> =
    map { erpModel ->
        val position = erpModel.position?.let { PositionErpModel(it.latitude, it.longitude) }
        PharmacyDetailsErpModel(
            id = erpModel.id ?: "",
            name = erpModel.name,
            address = erpModel.address.let { "${it?.lineAddress}\n${it?.postalCode} ${it?.city}" }.trim(),
            coordinates = position,
            distance = when (locationMode) {
                is LocationModeErpModel.Enabled -> position?.minus(locationMode.coordinates)
                else -> null
            },
            contact = erpModel.contact.let {
                ContactInformationErpModel(phone = it.phone, mail = it.mail, url = it.url)
            },
            provides = erpModel.extractServices(type),
            openingHours = erpModel.availableTime.toPharmacyOpeningHours(),
            specialClosingTimes = erpModel.notAvailablePeriodsWithMetadata(),
            specialOpeningTimes = erpModel.specialOpeningTimesWithMetadata(),
            telematikId = erpModel.telematikId,
            onSiteFeatures = erpModel.onSiteFeatures.sortedBy { it.code },
            availableServices = erpModel.availableServices.sortedBy { it.code }
        )
    }

fun OpeningHoursErpModel.toPharmacyOpeningHours(): PharmacyOpeningHoursErpModel =
    PharmacyOpeningHoursErpModel(
        openingTime = openingTime.mapValues { (_, times) ->
            times.map { PharmacyOpeningTimeErpModel(it.openingTime, it.closingTime) }
        }
    )

private fun FhirPharmacyErpModel.extractServices(type: PharmacyVzdService = FHIRVZD): List<PharmacyServiceErpModel> {
    val services = mutableListOf<PharmacyServiceErpModel>()

    val isOpeningHoursPresent = availableTime.isNotEmpty()

    val openingHours = if (isOpeningHoursPresent) availableTime.toPharmacyOpeningHours() else PharmacyOpeningHoursErpModel(emptyMap())

    val localServices = LocalPharmacyServiceErpModel(
        name = name,
        openingHours = openingHours
    )

    // adding the hours of operation as local services (this is used in the ui to decide the opening hours)
    services.add(localServices)

    services.addAll(
        specialities.mapNotNull { speciality ->
            when (speciality) {
                FhirVzdSpecialtyType.Pickup -> PickUpPharmacyServiceErpModel(name)
                FhirVzdSpecialtyType.Delivery -> DeliveryPharmacyServiceErpModel(
                    name = name,
                    openingHours = openingHours
                )

                FhirVzdSpecialtyType.Shipment -> OnlinePharmacyServiceErpModel(name)
                else -> null
            }
        }
    )

    return services.toList()
}
