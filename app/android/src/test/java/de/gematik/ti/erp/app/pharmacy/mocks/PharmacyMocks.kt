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

package de.gematik.ti.erp.app.pharmacy.mocks

import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_SYNCED_TASK_DATA_01
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_SYNCED_TASK_DATA_02
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

internal val MOCK_ACTIVE_PROFILE = ProfilesData.Profile(
    id = "1",
    name = "Mustermann",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.FemaleDoctor,
    insuranceIdentifier = "12345567890",
    insuranceType = ProfilesData.InsuranceType.GKV,
    insurantName = "Mustermann",
    insuranceName = "GesundheitsVersichert AG",
    singleSignOnTokenScope = null,
    active = false,
    isConsentDrawerShown = false,
    lastAuthenticated = null
)

internal val MOCK_SHIPPING_CONTACT = PharmacyData.ShippingContact(
    name = "Max Mustermann",
    line1 = "Musterstraße 1",
    line2 = "",
    postalCode = "12345",
    city = "Musterstadt",
    telephoneNumber = "0123456789",
    mail = "",
    deliveryInformation = ""
)

internal val MOCK_SCANNED_TASK_DATA_REDEEMABLE_01 = ScannedTaskData.ScannedTask(
    index = 1,
    name = "ScannedTaskName",
    profileId = "1",
    taskId = "1",
    accessCode = "1",
    redeemedOn = null,
    scannedOn = Clock.System.now()
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_01 = MOCK_SYNCED_TASK_DATA_01.copy(
    expiresOn = Clock.System.now().plus(1.days),
    acceptUntil = Clock.System.now().plus(1.days)
)

internal val MOCK_SCANNED_TASK_DATA_REDEEMABLE_02 = MOCK_SCANNED_TASK_DATA_REDEEMABLE_01.copy(
    index = 2
)

internal val MOCK_SCANNED_TASK_DATA_REDEEMED_01 = MOCK_SCANNED_TASK_DATA_REDEEMABLE_01.copy(
    index = 3,
    redeemedOn = Clock.System.now().minus(1.days)
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_02 = MOCK_SYNCED_TASK_DATA_02.copy(
    expiresOn = Clock.System.now().plus(1.days),
    acceptUntil = Clock.System.now().plus(1.days)
)

internal val MEDICATION = SyncedTaskData.Medication(
    category = SyncedTaskData.MedicationCategory.entries[0],
    vaccine = true,
    text = "MedicationName",
    form = "AEO",
    lotNumber = "1234567890",
    expirationDate = FhirTemporal.Instant(Instant.DISTANT_PAST),
    identifier = SyncedTaskData.Identifier("1234567890"),
    normSizeCode = "N1",
    ingredientMedications = emptyList(),
    manufacturingInstructions = null,
    packaging = null,
    ingredients = emptyList(),
    amount = Ratio(
        numerator = Quantity(
            value = "1",
            unit = "oz"
        ),
        denominator = null
    )
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_SELF_PAYER_03 = MOCK_SYNCED_TASK_DATA_02.copy(
    expiresOn = Clock.System.now().plus(1.days),
    acceptUntil = Clock.System.now().plus(1.days),
    insuranceInformation = SyncedTaskData.InsuranceInformation(
        name = "TestInsurance",
        status = "Active",
        coverageType = SyncedTaskData.CoverageType.SEL
    ),
    medicationRequest = SyncedTaskData.MedicationRequest(
        MEDICATION, null, null, SyncedTaskData.AccidentType.None,
        null, null, false, null,
        SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
    )
)
