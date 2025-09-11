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

import androidx.compose.ui.text.buildAnnotatedString
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek

val mockPharmacy = Pharmacy(
    id = "pharmacy",
    name = "Test - Pharmacy",
    address = null,
    coordinates = null,
    distance = null,
    contact = PharmacyUseCaseData.PharmacyContact(
        phone = "0123456",
        mail = "mail@mail.com",
        url = "https://website.com"
    ),
    provides = listOf(),
    openingHours = null,
    telematikId = "telematik-id"
)

val openingTimeA = PharmacyUseCaseData.OpeningTime(
    LocalTime.parse("08:00:00"),
    LocalTime.parse("12:00:00")
)
val openingTimeB = PharmacyUseCaseData.OpeningTime(
    LocalTime.parse("14:00:00"),
    LocalTime.parse("18:00:00")
)

val mockOpeningHours = PharmacyUseCaseData.OpeningHours(
    openingTime = mapOf(
        DayOfWeek.MONDAY to listOf(openingTimeA, openingTimeB),
        DayOfWeek.TUESDAY to listOf(openingTimeA, openingTimeB),
        DayOfWeek.WEDNESDAY to listOf(openingTimeA, openingTimeB),
        DayOfWeek.THURSDAY to listOf(openingTimeA, openingTimeB),
        DayOfWeek.FRIDAY to listOf(openingTimeA, openingTimeB),
        DayOfWeek.SATURDAY to listOf(openingTimeA)
    )
)

val mockDetailedInfoText = buildAnnotatedString {
    append("Detailed information about the pharmacy's services and policies.")
}
