/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.model.Coordinates
import de.gematik.ti.erp.app.fhir.model.OpeningHours
import de.gematik.ti.erp.app.fhir.model.OpeningTime
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.fhir.model.PharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek

class PharmacyPreviewParameterProvider : PreviewParameterProvider<PharmacyUseCaseData.Pharmacy> {
    override val values = sequenceOf(
        PharmacyPreviewData.ALL_PRESENT_DATA,
        PharmacyPreviewData.PICK_UP_ONLY_DATA,
        PharmacyPreviewData.DELIVERY_PICKUP_ONLY_DATA,
        PharmacyPreviewData.LOCAL_PICKUP_ONLY_DATA
    )
}

class PharmacySheetFromMessagesParameterProvider : PreviewParameterProvider<PharmacyUseCaseData.Pharmacy> {
    override val values = sequenceOf(
        PharmacyPreviewData.PHONE_CONTACT_ONLY,
        PharmacyPreviewData.MAIL_CONTACT_ONLY,
        PharmacyPreviewData.LOCATION_CONTACT_ONLY,
        PharmacyPreviewData.LOCATION_PHONE_CONTACT,
        PharmacyPreviewData.ALL_CONTACT
    )
}

object PharmacyPreviewData {

    private val openingHoursSample = OpeningHours(
        openingTime = mapOf(
            DayOfWeek.SATURDAY to listOf(
                OpeningTime(
                    openingTime = LocalTime(8, 0, 0),
                    closingTime = LocalTime(23, 0, 0)
                )
            )
        )
    )

    val ALL_PRESENT_DATA = PharmacyUseCaseData.Pharmacy(
        id = "1",
        name = "Apotheke am Markt",
        address = "Marktstraße 1, 12345 Musterstadt",
        coordinates = Coordinates(0.0, 0.0),
        distance = 1.0,
        provides = listOf(
            PharmacyService.OnlinePharmacyService(name = "Online"),
            PharmacyService.PickUpPharmacyService(name = "PickUp"),
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            ),
            PharmacyService.DeliveryPharmacyService(
                name = "Delivery",
                openingHours = openingHoursSample
            )
        ),
        openingHours = OpeningHours(
            openingTime = mapOf(
                DayOfWeek.SATURDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(20, 0, 0)
                    )
                ),
                DayOfWeek.SUNDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(11, 0, 0),
                        closingTime = LocalTime(15, 0, 0)
                    )
                ),
                DayOfWeek.MONDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(23, 0, 0)
                    )
                ),
                DayOfWeek.TUESDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(23, 0, 0)
                    )
                ),
                DayOfWeek.WEDNESDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(23, 0, 0)
                    )
                ),
                DayOfWeek.THURSDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(23, 0, 0)
                    )
                ),
                DayOfWeek.FRIDAY to listOf(
                    OpeningTime(
                        openingTime = LocalTime(8, 0, 0),
                        closingTime = LocalTime(23, 0, 0)
                    )
                )
            )
        ),
        telematikId = "123456789",
        contacts = PharmacyContacts(
            phone = "0123456789",
            mail = "mpq@nrw.de",
            url = "www.apotheke-am-markt.de",
            deliveryUrl = "www.apotheke-am-markt.de/lieferung",
            onlineServiceUrl = "www.apotheke-am-markt.de/online",
            pickUpUrl = "www.apotheke-am-markt.de/abholung"
        )
    )

    val PICK_UP_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.PickUpPharmacyService(name = "PickUp")
        ),
        contacts = PharmacyContacts(
            phone = "0123456789",
            mail = "",
            url = "",
            deliveryUrl = "",
            onlineServiceUrl = "",
            pickUpUrl = ""
        )
    )

    val DELIVERY_PICKUP_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.DeliveryPharmacyService(
                name = "Delivery",
                openingHours = openingHoursSample
            )
        ),
        contacts = PharmacyContacts(
            phone = "0123456789",
            mail = "",
            url = "",
            deliveryUrl = "www.apotheke-am-markt.de/lieferung",
            onlineServiceUrl = "",
            pickUpUrl = "www.apotheke-am-markt.de/abholung"
        )
    )

    val LOCAL_PICKUP_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        ),
        contacts = PharmacyContacts(
            phone = "0123456789",
            mail = "",
            url = "",
            deliveryUrl = "www.apotheke-am-markt.de/lieferung",
            onlineServiceUrl = "",
            pickUpUrl = "www.apotheke-am-markt.de/abholung"
        )
    )

    val PHONE_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        ),
        coordinates = null,
        contacts = PharmacyContacts(
            phone = "0123456789",
            mail = "",
            url = "",
            deliveryUrl = "",
            onlineServiceUrl = "",
            pickUpUrl = ""
        )
    )

    val MAIL_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        ),
        coordinates = null,
        contacts = PharmacyContacts(
            phone = "",
            mail = "pharm@pharm.de",
            url = "",
            deliveryUrl = "",
            onlineServiceUrl = "",
            pickUpUrl = ""
        )
    )

    val LOCATION_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        ),
        contacts = PharmacyContacts(
            phone = "",
            mail = "",
            url = "",
            deliveryUrl = "",
            onlineServiceUrl = "",
            pickUpUrl = ""
        )
    )

    val LOCATION_PHONE_CONTACT = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        ),
        contacts = PharmacyContacts(
            phone = "123",
            mail = "",
            url = "",
            deliveryUrl = "",
            onlineServiceUrl = "",
            pickUpUrl = ""
        )
    )

    val ALL_CONTACT = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = openingHoursSample
            )
        )
    )
}
