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

import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPositionErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Delivery
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Pickup
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Shipment
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningTimeErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

private val testBundle by lazy { File("$ResourceBasePath/pharmacy_result_bundle.json").readText() }
private val testBundleBinaries by lazy { File("$ResourceBasePath/fhir/pharmacy_binary.json").readText() }

class PharmacyMapperTest {

    @Test
    fun `extract certificate`() {
        val result = extractBinaryCertificatesAsBase64(Json.parseToJsonElement(testBundleBinaries))
        assertEquals(listOf("MIIFlDCCBHygAwwKGi44czSg=="), result)
    }

    companion object {

        private val openingTimeAErpModel = OpeningTimeErpModel(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"))
        private val openingTimeBErpModel = OpeningTimeErpModel(LocalTime.parse("14:00:00"), LocalTime.parse("18:00:00"))
        private val openingTimeCErpModel = OpeningTimeErpModel(LocalTime.parse("08:00:00"), LocalTime.parse("20:00:00"))

        private val openingTimeAUseCaseModel = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"))
        private val openingTimeBUseCaseModel = OpeningTime(LocalTime.parse("14:00:00"), LocalTime.parse("18:00:00"))
        private val openingTimeCUseCaseModel = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("20:00:00"))

        val pharmacyErpModel = FhirPharmacyErpModel(
            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
            name = "Heide-Apotheke",
            telematikId = "3-05.2.1007600000.080",
            position = FhirPositionErpModel(
                latitude = 8.597412,
                longitude = 53.590027
            ),
            address = FhirPharmacyAddressErpModel(
                lineAddress = "",
                postalCode = "27578",
                city = "Bremerhaven"
            ),
            contact = FhirContactInformationErpModel(
                phone = "0471/87029",
                mail = "info@heide-apotheke-bremerhaven.de",
                url = "http://www.heide-apotheke-bremerhaven.de"
            ),
            specialities = listOf(Delivery, Pickup, Shipment),
            hoursOfOperation = OpeningHoursErpModel(
                openingTime = mapOf(
                    DayOfWeek.MONDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                    DayOfWeek.TUESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                    DayOfWeek.WEDNESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                    DayOfWeek.THURSDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                    DayOfWeek.FRIDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                    DayOfWeek.SATURDAY to listOf(openingTimeAErpModel)
                )
            ),
            availableTime = OpeningHoursErpModel(
                openingTime = mapOf(
                    DayOfWeek.MONDAY to listOf(openingTimeCErpModel),
                    DayOfWeek.TUESDAY to listOf(openingTimeCErpModel),
                    DayOfWeek.WEDNESDAY to listOf(openingTimeCErpModel),
                    DayOfWeek.THURSDAY to listOf(openingTimeCErpModel),
                    DayOfWeek.FRIDAY to listOf(openingTimeCErpModel)
                )
            )
        )

        val pharmacyUseCaseData = PharmacyUseCaseData.Pharmacy(
            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
            name = "Heide-Apotheke",
            address = "27578 Bremerhaven",
            coordinates = PharmacyUseCaseData.Coordinates(latitude = 8.597412, longitude = 53.590027),
            distance = null, // No distance provided
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "0471/87029",
                mail = "info@heide-apotheke-bremerhaven.de",
                url = "http://www.heide-apotheke-bremerhaven.de"
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.LocalPharmacyService(
                    name = "Heide-Apotheke",
                    openingHours = PharmacyUseCaseData.OpeningHours(
                        openingTime = mapOf(
                            DayOfWeek.MONDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                            DayOfWeek.TUESDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                            DayOfWeek.WEDNESDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                            DayOfWeek.THURSDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                            DayOfWeek.FRIDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                            DayOfWeek.SATURDAY to listOf(openingTimeAUseCaseModel)
                        )
                    )
                ),
                PharmacyUseCaseData.PharmacyService.DeliveryPharmacyService(
                    name = "Heide-Apotheke",
                    openingHours = PharmacyUseCaseData.OpeningHours(
                        openingTime = mapOf(
                            DayOfWeek.MONDAY to listOf(openingTimeCUseCaseModel),
                            DayOfWeek.TUESDAY to listOf(openingTimeCUseCaseModel),
                            DayOfWeek.WEDNESDAY to listOf(openingTimeCUseCaseModel),
                            DayOfWeek.THURSDAY to listOf(openingTimeCUseCaseModel),
                            DayOfWeek.FRIDAY to listOf(openingTimeCUseCaseModel)
                        )
                    )
                ),
                PharmacyUseCaseData.PharmacyService.PickUpPharmacyService(name = "Heide-Apotheke"),
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(name = "Heide-Apotheke")
            ),
            openingHours = PharmacyUseCaseData.OpeningHours(
                openingTime = mapOf(
                    DayOfWeek.MONDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                    DayOfWeek.TUESDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                    DayOfWeek.WEDNESDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                    DayOfWeek.THURSDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                    DayOfWeek.FRIDAY to listOf(openingTimeAUseCaseModel, openingTimeBUseCaseModel),
                    DayOfWeek.SATURDAY to listOf(openingTimeAUseCaseModel)
                )
            ),
            telematikId = "3-05.2.1007600000.080"
        )
    }
}
