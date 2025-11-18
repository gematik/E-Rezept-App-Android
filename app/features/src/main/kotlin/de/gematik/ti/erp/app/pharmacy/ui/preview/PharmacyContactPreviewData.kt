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
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModelPeriod
import de.gematik.ti.erp.app.fhir.pharmacy.model.NotAvailablePeriodErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.NotAvailablePeriodMetadata
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeMetadata
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek

val mockSpecialClosingTimes = listOf(
    NotAvailablePeriodMetadata(
        erpModel = NotAvailablePeriodErpModel(
            description = "Urlaub",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDateTime(LocalDateTime.parse("2028-10-13T12:30:00")), // Monday
                end = FhirTemporal.LocalDateTime(LocalDateTime.parse("2028-10-15T12:30:00")) // Wednesday
            )
        ),
        hasOverlap = false,
        isInPast = false,
        isActive = false
    ),
    NotAvailablePeriodMetadata(
        erpModel = NotAvailablePeriodErpModel(
            description = "RosenMontag",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDateTime(LocalDateTime.parse("2028-10-06T12:30:00")), // Monday
                end = FhirTemporal.LocalDateTime(LocalDateTime.parse("2028-10-08T12:30:00")) // Wednesday
            )
        ),
        hasOverlap = false,
        isInPast = false,
        isActive = true
    ),
    NotAvailablePeriodMetadata(
        erpModel = NotAvailablePeriodErpModel(
            description = "Urlaub 2",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate.parse("2028-11-13")), // Thursday
                end = FhirTemporal.LocalDate(LocalDate.parse("2028-11-15")) // Saturday
            )
        ),
        hasOverlap = true,
        isInPast = false,
        isActive = false
    )
)

val mockSpecialOpeningTimes = listOf(
    // 19:00-08:00 (show MOON)
    SpecialOpeningTimeMetadata(
        erpModel = SpecialOpeningTimeErpModel(
            description = "Notdienst",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Instant(Instant.parse("2026-11-03T19:00:00Z")),
                end = FhirTemporal.Instant(Instant.parse("2026-11-04T08:00:00Z"))
            )
        ),
        isInPast = false,
        isActive = true
    ),
    // 23:00-06:00 (show MOON)
    SpecialOpeningTimeMetadata(
        erpModel = SpecialOpeningTimeErpModel(
            description = "Notdienst",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Instant(Instant.parse("2026-12-24T23:00:00Z")),
                end = FhirTemporal.Instant(Instant.parse("2026-12-25T06:00:00Z"))
            )
        ),
        isInPast = false,
        isActive = false
    ),
    // 14:00-16:00 (show BELL)
    SpecialOpeningTimeMetadata(
        erpModel = SpecialOpeningTimeErpModel(
            description = "Sonderöffnung",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Instant(Instant.parse("2026-11-25T14:00:00Z")),
                end = FhirTemporal.Instant(Instant.parse("2026-11-25T16:00:00Z"))
            )
        ),
        isInPast = false,
        isActive = false
    ),
    // Date only period (show BELL)
    SpecialOpeningTimeMetadata(
        erpModel = SpecialOpeningTimeErpModel(
            description = "Sonderöffnung",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate.parse("2026-12-01")),
                end = FhirTemporal.LocalDate(LocalDate.parse("2026-12-03"))
            )
        ),
        isInPast = false,
        isActive = false
    )
)

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
    specialClosingTimes = mockSpecialClosingTimes,
    specialOpeningTimes = mockSpecialOpeningTimes,
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
