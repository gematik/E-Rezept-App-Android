/*
 * Copyright (c) 2024 gematik GmbH
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

import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import java.io.File
import java.time.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

private val testBundle by lazy { File("$ResourceBasePath/pharmacy_result_bundle.json").readText() }
private val testBundleBinaries by lazy { File("$ResourceBasePath/fhir/pharmacy_binary.json").readText() }
private val directRedeemPharmacyBundle by lazy {
    File("$ResourceBasePath/direct_redeem_pharmacy_bundle.json").readText()
}

class PharmacyMapperTest {
    private val openingTimeA = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"))
    private val openingTimeB = OpeningTime(LocalTime.parse("14:00:00"), LocalTime.parse("18:00:00"))
    private val openingTimeC = OpeningTime(LocalTime.parse("08:00:00"), LocalTime.parse("20:00:00"))
    private val expected = Pharmacy(
        id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
        name = "Heide-Apotheke",
        address = PharmacyAddress(
            lines = listOf("Langener Landstraße 266"),
            postalCode = "27578",
            city = "Bremerhaven"
        ),
        location = Location(latitude = 8.597412, longitude = 53.590027),
        contacts = PharmacyContacts(
            phone = "0471/87029",
            mail = "info@heide-apotheke-bremerhaven.de",
            url = "http://www.heide-apotheke-bremerhaven.de",
            pickUpUrl = "",
            deliveryUrl = "",
            onlineServiceUrl = ""
        ),
        provides = listOf(
            LocalPharmacyService(
                name = "Heide-Apotheke",
                openingHours = OpeningHours(
                    openingTime = mapOf(
                        DayOfWeek.MONDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.TUESDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.WEDNESDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.THURSDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.FRIDAY to listOf(openingTimeA, openingTimeB),
                        DayOfWeek.SATURDAY to listOf(openingTimeA)
                    )
                )
            ),
            DeliveryPharmacyService(
                name = "Heide-Apotheke",
                openingHours = OpeningHours(
                    openingTime = mapOf(
                        DayOfWeek.MONDAY to listOf(openingTimeC),
                        DayOfWeek.TUESDAY to listOf(openingTimeC),
                        DayOfWeek.WEDNESDAY to listOf(openingTimeC),
                        DayOfWeek.THURSDAY to listOf(openingTimeC),
                        DayOfWeek.FRIDAY to listOf(openingTimeC)
                    )
                )
            ),
            OnlinePharmacyService(
                name = "Heide-Apotheke"
            ),
            PickUpPharmacyService(
                name = "Heide-Apotheke"
            )
        ),
        telematikId = "3-05.2.1007600000.080"
    )

    private val expectedDirectRedeemPharmacy =
        Pharmacy(
            id = "ngc26fe2-9c3a-4d52-854e-794c96f73f66",
            name = "PT-STA-Apotheke 2TEST-ONLY",
            address = PharmacyAddress(
                lines = listOf("Münchnerstr. 15 b"),
                postalCode = "82139",
                city = "Starnberg"
            ),
            location = Location(latitude = 48.0018513, longitude = 11.3497755),
            contacts = PharmacyContacts(
                phone = "",
                mail = "",
                url = "",
                pickUpUrl = "https://ixosapi.service-pt.de/api/GematikAppZuweisung/945357?sig=Co" +
                    "LEeMyykSQul06Rp4wyTsfOJPBrSHOG2YBB4Bzy8QQ%3d&se=2625644988",
                deliveryUrl = "https://ixosapi.service-pt.de/api/GematikAppZuweisung/945357?sig=CoLEeM" +
                    "yykSQul06Rp4wyTsfOJPBrSHOG2YBB4Bzy8QQ%3d&se=2625644988",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                LocalPharmacyService(
                    name = "PT-STA-Apotheke 2TEST-ONLY",
                    openingHours = OpeningHours(mapOf())
                ),
                OnlinePharmacyService(name = "PT-STA-Apotheke 2TEST-ONLY"),
                PickUpPharmacyService(name = "PT-STA-Apotheke 2TEST-ONLY")
            ),
            telematikId = "3-SMC-B-Testkarte-883110000116948"
        )

    @Test
    fun `map pharmacies from JSON bundle`() {
        val pharmacies = extractPharmacyServices(
            Json.parseToJsonElement(testBundle),
            onError = { element, cause ->
                println(element)
                throw cause
            }
        ).pharmacies

        assertEquals(10, pharmacies.size)

        assertEquals(expected, pharmacies[0])
    }

    @Test
    fun `map direct redeem pharmacy from JSON bundle get urls for direct redeem`() {
        val pharmacies = extractPharmacyServices(
            Json.parseToJsonElement(directRedeemPharmacyBundle),
            onError = { element, cause ->
                println(element)
                throw cause
            }
        ).pharmacies

        assertEquals(1, pharmacies.size)

        assertEquals(expectedDirectRedeemPharmacy, pharmacies[0])
    }

    @Test
    fun `extract certificate`() {
        val result = extractBinaryCertificatesAsBase64(Json.parseToJsonElement(testBundleBinaries))
        assertEquals(listOf("MIIFlDCCBHygAwwKGi44czSg=="), result)
    }
}
