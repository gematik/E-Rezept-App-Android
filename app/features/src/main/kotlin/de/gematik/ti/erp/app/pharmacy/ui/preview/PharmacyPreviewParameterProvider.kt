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

package de.gematik.ti.erp.app.pharmacy.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningHours
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningTime
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyContact
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek

class PharmacyPreviewParameterProvider : PreviewParameterProvider<PharmacyUseCaseData.Pharmacy> {
    override val values = sequenceOf(
        PharmacyPreviewData.ALL_PRESENT_DATA,
        PharmacyPreviewData.PICK_UP_ONLY_DATA,
        PharmacyPreviewData.PICK_UP_AND_DELIVERY_DATA,
        PharmacyPreviewData.DELIVERY_PICKUP_ONLY_DATA,
        PharmacyPreviewData.ONLINE_ONLY_DATA
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
        specialClosingTimes = mockSpecialClosingTimes,
        specialOpeningTimes = mockSpecialOpeningTimes,
        telematikId = "123456789",
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "mpq@nrw.de",
            url = "www.apotheke-am-markt.de"
        )
    )

    val PICK_UP_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.PickUpPharmacyService(name = "PickUp")
        ),
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "",
            url = ""
        )
    )

    val PICK_UP_AND_DELIVERY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.PickUpPharmacyService(name = "PickUp"),
            PharmacyService.DeliveryPharmacyService(
                name = "Delivery",
                openingHours = openingHoursSample
            )
        ),
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "",
            url = ""
        )
    )

    val DELIVERY_PICKUP_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.DeliveryPharmacyService(
                name = "Delivery",
                openingHours = openingHoursSample
            )
        ),
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "",
            url = ""
        )
    )

    val ONLINE_ONLY_DATA = ALL_PRESENT_DATA.copy(
        provides = listOf(
            PharmacyService.OnlinePharmacyService(name = "Online")
        ),
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "",
            url = ""
        )
    )

    val PHONE_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        coordinates = null,
        contact = PharmacyContact(
            phone = "0123456789",
            mail = "",
            url = ""
        )
    )

    val MAIL_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        coordinates = null,
        contact = PharmacyContact(
            phone = "",
            mail = "pharm@pharm.de",
            url = ""
        )
    )

    val LOCATION_CONTACT_ONLY = ALL_PRESENT_DATA.copy(
        contact = PharmacyContact(
            phone = "",
            mail = "",
            url = ""
        )
    )

    val LOCATION_PHONE_CONTACT = ALL_PRESENT_DATA.copy(
        contact = PharmacyContact(
            phone = "123",
            mail = "",
            url = ""
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
