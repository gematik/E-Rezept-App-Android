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

package de.gematik.ti.erp.app.medicationplan

import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.mockk.mockk
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

val scannedTaskAmount = RatioErpModel(QuantityErpModel("1", ""), QuantityErpModel("1", ""))
val syncedTaskAmount = RatioErpModel(QuantityErpModel("1", ""), QuantityErpModel("1", ""))

val MEDICATION_SCHEDULE = MedicationScheduleErpModel(
    duration = MedicationScheduleDurationErpModel.Personalized(
        startDate = Instant.parse("2024-01-01T00:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = MedicationScheduleIntervalErpModel.Daily,
    isActive = true,
    message = MedicationNotificationMessageErpModel("title", "body"),
    taskId = "taskId",
    profileId = "profileId",
    amount = scannedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotificationErpModel(
            time = LocalTime(8, 0),
            dosage = MedicationScheduleNotificationDosageErpModel("tablet", "1"),
            id = "1234"
        )
    )
)

val profile1 = ProfileErpModel(
    id = "PROFILE_ID1",
    name = "Erna Mustermann",
    active = true,
    isNewlyCreated = false,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = null
    ),
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Erna Mustermann",
        insuranceIdentifier = "AOK",
        insuranceName = null,
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = null
    ),
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastAuditEventSynced = null,
    lastTaskSynced = mockk(),
    userAuthentication = UserAuthenticationErpModel.NotInitialized
)

val profile2 = ProfileErpModel(
    id = "PROFILE_ID2",
    name = "Erna P",
    active = true,
    isNewlyCreated = false,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = null
    ),
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Erna P",
        insuranceIdentifier = "AOK",
        insuranceName = null,
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = null
    ),
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastAuditEventSynced = null,
    lastTaskSynced = mockk(),
    userAuthentication = UserAuthenticationErpModel.NotInitialized
)

val medicationScheduleErpModel1 = MedicationScheduleErpModel(
    duration = MedicationScheduleDurationErpModel.Personalized(
        startDate = Instant.parse("2024-01-01T08:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = MedicationScheduleIntervalErpModel.Daily,
    isActive = true,
    message = MedicationNotificationMessageErpModel("title", "body"),
    taskId = "taskId",
    profileId = "PROFILE_ID1",
    amount = scannedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotificationErpModel(id = "1", time = LocalTime(8, 0), dosage = MedicationScheduleNotificationDosageErpModel("Dosis", "1")),
        MedicationScheduleNotificationErpModel(id = "2", time = LocalTime(12, 0), dosage = MedicationScheduleNotificationDosageErpModel("Dosis", "1"))
    )
)

val medicationScheduleErpModel2 = MedicationScheduleErpModel(
    duration = MedicationScheduleDurationErpModel.Personalized(
        startDate = Instant.parse("2024-01-01T08:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = MedicationScheduleIntervalErpModel.Daily,
    isActive = true,
    message = MedicationNotificationMessageErpModel("title", "body"),
    taskId = "taskId",
    profileId = "PROFILE_ID2",
    amount = syncedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotificationErpModel(id = "3", time = LocalTime(10, 5), dosage = MedicationScheduleNotificationDosageErpModel("Dosis", "1")),
        MedicationScheduleNotificationErpModel(id = "4", time = LocalTime(18, 0), dosage = MedicationScheduleNotificationDosageErpModel("Dosis", "1"))
    )
)

val scannedTask = API_ACTIVE_SCANNED_TASK

internal var MEDICATION_REQUEST: MedicationRequestErpModel = requireNotNull(API_ACTIVE_SYNCED_TASK.medicationRequest)

val syncedTask = API_ACTIVE_SYNCED_TASK
