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
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.APOVZD
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates.Companion.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningHours
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningHours.Companion.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyContact.Companion.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.OnlinePharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.PickUpPharmacyService

// can't be modified; the backend will always return 80 entries on the first page
const val PharmacyInitialResultsPerPage = 80 // making it from 80 to 100 to match the backend
const val PharmacyNextResultsPerPage = 10

fun List<FhirPharmacyErpModel>.toModel(
    locationMode: PharmacyUseCaseData.LocationMode? = null,
    type: PharmacyVzdService
): List<PharmacyUseCaseData.Pharmacy> =
    map { erpModel ->
        PharmacyUseCaseData.Pharmacy(
            id = erpModel.id ?: "",
            name = erpModel.name,
            address = erpModel.address.let { "${it?.lineAddress}\n${it?.postalCode} ${it?.city}" }.trim(),
            coordinates = erpModel.position?.toModel(),
            distance = when (locationMode) {
                is PharmacyUseCaseData.LocationMode.Enabled -> erpModel.position?.toModel()?.minus(locationMode.coordinates)
                else -> null
            },
            contact = erpModel.contact.toModel(),
            provides = erpModel.extractServices(type),
            openingHours = if (type == APOVZD) erpModel.hoursOfOperation?.toModel() else erpModel.availableTime.toModel(),
            telematikId = erpModel.telematikId
        )
    }

private fun FhirPharmacyErpModel.extractServices(type: PharmacyVzdService): List<PharmacyUseCaseData.PharmacyService> {
    val services = mutableListOf<PharmacyUseCaseData.PharmacyService>()

    val isOpeningHoursPresent = availableTime.isNotEmpty()

    val localServices = PharmacyUseCaseData.PharmacyService.LocalPharmacyService(
        name = name,
        openingHours = when (type) {
            APOVZD -> hoursOfOperation?.toModel() ?: OpeningHours(emptyMap())
            FHIRVZD -> if (isOpeningHoursPresent) availableTime.toModel() else OpeningHours(emptyMap())
        }
    )

    // adding the hours of operation as local services (this is used in the ui to decide the opening hours)
    services.add(localServices)

    services.addAll(
        specialities.mapNotNull { speciality ->
            when (speciality) {
                FhirVzdSpecialtyType.Pickup -> PickUpPharmacyService(name)
                FhirVzdSpecialtyType.Delivery -> DeliveryPharmacyService(
                    name = name,
                    openingHours = if (isOpeningHoursPresent) availableTime.toModel() else OpeningHours(emptyMap())
                )

                FhirVzdSpecialtyType.Shipment -> OnlinePharmacyService(name)
                else -> null
            }
        }
    )

    return services.toList()
}

fun PharmacyData.ShippingContact.toModel() =
    PharmacyUseCaseData.ShippingContact(
        name = name,
        line1 = line1,
        line2 = line2,
        postalCode = postalCode,
        city = city,
        telephoneNumber = telephoneNumber,
        mail = mail,
        deliveryInformation = deliveryInformation
    )
