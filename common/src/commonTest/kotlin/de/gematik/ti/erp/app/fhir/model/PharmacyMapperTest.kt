/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPositionErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirVzdSpecialtyType.Delivery
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirVzdSpecialtyType.Pickup
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirVzdSpecialtyType.Shipment
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
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
private val directRedeemPharmacyBundle by lazy {
    File("$ResourceBasePath/direct_redeem_pharmacy_bundle.json").readText()
}

class PharmacyMapperTest {
    @Test
    fun `map pharmacies from JSON bundle`() {
        val pharmacies = extractPharmacyServices(
            Json.parseToJsonElement(testBundle),
            onError = { element, cause ->
                println(element)
                throw cause
            }
        )

        assertEquals(10, pharmacies.total)
        assertEquals(pharmacyErpModel, pharmacies.entries.first())
        val useCaseData = pharmacies.entries.toModel(type = PharmacyVzdService.APOVZD)
        assertEquals(pharmacyUseCaseData, useCaseData.first())
    }

    @Test
    fun `map direct redeem pharmacy from JSON bundle get urls for direct redeem`() {
        val pharmacies = extractPharmacyServices(
            Json.parseToJsonElement(directRedeemPharmacyBundle),
            onError = { element, cause ->
                println(element)
                throw cause
            }
        )

        assertEquals(1, pharmacies.total)
        assertEquals(directRedeemPharmacyErpModel, pharmacies.entries.first())
        val useCaseData = pharmacies.entries.toModel(type = PharmacyVzdService.APOVZD)
        println(useCaseData.first())
    }

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
            address = FhirAddressErpModel(
                lineAddress = "",
                postalCode = "27578",
                city = "Bremerhaven"
            ),
            contact = FhirContactInformationErpModel(
                phone = "0471/87029",
                mail = "info@heide-apotheke-bremerhaven.de",
                url = "http://www.heide-apotheke-bremerhaven.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
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
                url = "http://www.heide-apotheke-bremerhaven.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
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

        val directRedeemPharmacyErpModel = FhirPharmacyErpModel(
            id = "ngc26fe2-9c3a-4d52-854e-794c96f73f66",
            name = "PT-STA-Apotheke 2TEST-ONLY",
            telematikId = "3-SMC-B-Testkarte-883110000116948",
            position = FhirPositionErpModel(
                latitude = 48.0018513,
                longitude = 11.3497755
            ),
            address = FhirAddressErpModel(
                lineAddress = "",
                postalCode = "82139",
                city = "Starnberg"
            ),
            contact = FhirContactInformationErpModel(
                phone = "",
                mail = "",
                url = "",
                pickUpUrl = "https://ixosapi.service-pt.de/api/GematikAppZuweisung/945357?sig=CoLEeMyykSQul06Rp4wyTsfOJPBrSHOG2YBB4Bzy8QQ%3d&se=2625644988",
                deliveryUrl = "https://ixosapi.service-pt.de/api/GematikAppZuweisung/945357?sig=CoLEeMyykSQul06Rp4wyTsfOJPBrSHOG2YBB4Bzy8QQ%3d&se=2625644988",
                onlineServiceUrl = ""
            ),
            specialities = listOf(Pickup, Shipment),
            availableTime = OpeningHoursErpModel(
                openingTime = emptyMap() // No opening times provided
            )
        )
    }
}
