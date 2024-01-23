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

package de.gematik.ti.erp.app.utils

import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun syncedTask(
    taskId: String = "Task/" + UUID.randomUUID().toString(),
    accessCode: String = UUID.randomUUID().toString(),
    lastModified: Instant,
    organizationName: String?,
    practitionerName: String?,
    expiresOn: Instant?,
    acceptUntil: Instant?,
    authoredOn: Instant,
    status: SyncedTaskData.TaskStatus,
    medicationName: String,
    medicationDispenseWhenHandedOver: FhirTemporal? = null
) =
    SyncedTaskData.SyncedTask(
        profileId = "",
        taskId = taskId,
        isIncomplete = false,
        pvsIdentifier = "123456",
        accessCode = accessCode,
        lastModified = lastModified,
        organization = SyncedTaskData.Organization(
            name = organizationName,
            address = null,
            uniqueIdentifier = null,
            phone = null,
            mail = null
        ),
        practitioner = SyncedTaskData.Practitioner(
            name = practitionerName,
            qualification = null,
            practitionerIdentifier = null
        ),
        patient = SyncedTaskData.Patient(
            name = null,
            address = null,
            birthdate = null,
            insuranceIdentifier = null
        ),
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = null,
            status = null
        ),
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        authoredOn = authoredOn,
        status = status,
        medicationRequest = SyncedTaskData.MedicationRequest(
            medication = SyncedTaskData.MedicationPZN(
                category = SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
                vaccine = false,
                text = medicationName,
                form = null,
                lotNumber = null,
                expirationDate = null,
                uniqueIdentifier = "",
                normSizeCode = null,
                amount = SyncedTaskData.Ratio(
                    numerator = SyncedTaskData.Quantity(
                        value = "",
                        unit = ""
                    ),
                    denominator = null
                )
            ),
            dateOfAccident = null,
            location = null,
            emergencyFee = null,
            substitutionAllowed = false,
            dosageInstruction = null,
            note = "",
            multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo()
        ),
        medicationDispenses = if (medicationDispenseWhenHandedOver != null) {
            listOf(
                SyncedTaskData.MedicationDispense(
                    dispenseId = null,
                    patientIdentifier = "",
                    medication = null,
                    wasSubstituted = false,
                    dosageInstruction = "",
                    performer = "",
                    whenHandedOver = medicationDispenseWhenHandedOver
                )
            )
        } else {
            emptyList()
        },
        communications = listOf(),
        failureToReport = "abcdefg"
    )

val testSyncedTasks =
    listOf(
        // 0
        syncedTask(
            lastModified = Instant.parse("2020-12-06T14:49:46Z"),
            organizationName = null,
            practitionerName = "Praxis Glücklicher gehts nicht",
            expiresOn = Instant.parse("2020-12-02T14:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-02T14:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-02T14:49:46Z"),
            status = SyncedTaskData.TaskStatus.Completed,
            medicationName = "Schokolade"
        ),
        // 1
        syncedTask(
            lastModified = Instant.parse("2020-12-05T14:49:46Z"),
            organizationName = null,
            practitionerName = "Praxis Glücklicher gehts nicht",
            expiresOn = Instant.parse("2020-12-02T22:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-02T14:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-02T14:49:46Z"),
            status = SyncedTaskData.TaskStatus.Completed,
            medicationName = "Bonbons"
        ),
        // 2
        syncedTask(
            lastModified = Instant.parse("2020-12-05T09:49:46Z"),
            organizationName = null,
            practitionerName = "Praxis Glücklicher gehts nicht",
            expiresOn = Instant.parse("2020-12-02T14:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-02T14:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-05T09:49:46Z"),
            status = SyncedTaskData.TaskStatus.Ready,
            medicationName = "Gummibärchen"
        ),
        // 3
        syncedTask(
            lastModified = Instant.parse("2020-12-20T09:49:46Z"),
            organizationName = "MVZ Haus der vielen Ärzte",
            practitionerName = null,
            expiresOn = Instant.parse("2020-12-20T09:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-20T09:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-20T09:49:46Z"),
            status = SyncedTaskData.TaskStatus.Ready,
            medicationName = "Viel zu viel"
        ),
        // 4
        syncedTask(
            lastModified = Instant.parse("2020-12-04T09:49:46Z"),
            organizationName = "MVZ Haus der vielen Ärzte",
            practitionerName = null,
            expiresOn = Instant.parse("2020-12-04T09:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-04T09:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-04T09:49:46Z"),
            status = SyncedTaskData.TaskStatus.Ready,
            medicationName = "Viel zu viel"
        ),
        // 5
        syncedTask(
            lastModified = Clock.System.now().plus(10.minutes),
            organizationName = "MVZ Haus der vielen Ärzte",
            practitionerName = null,
            expiresOn = Instant.parse("2020-12-04T09:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-04T09:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-04T09:49:46Z"),
            status = SyncedTaskData.TaskStatus.Completed,
            medicationName = "Viel zu viel"
        ),
        // 6
        syncedTask(
            lastModified = Clock.System.now().plus(1.minutes),
            organizationName = "MVZ Haus der vielen Ärzte",
            practitionerName = null,
            expiresOn = Instant.parse("2020-12-04T09:49:46Z") + (3 * 28).days,
            acceptUntil = Instant.parse("2020-12-04T09:49:46Z") + 28.days,
            authoredOn = Instant.parse("2020-12-04T09:49:46Z"),
            status = SyncedTaskData.TaskStatus.Completed,
            medicationName = "Viel zu viel"
        )
    )

fun scannedTask(
    taskId: String = "Task/" + UUID.randomUUID().toString(),
    accessCode: String = UUID.randomUUID().toString(),
    scannedOn: Instant,
    redeemedOn: Instant?,
    sentOn: Instant?
) =
    ScannedTaskData.ScannedTask(
        profileId = "",
        taskId = taskId,
        accessCode = accessCode,
        scannedOn = scannedOn,
        redeemedOn = redeemedOn
    )

val testScannedTasks =
    listOf(
        // 0
        scannedTask(
            scannedOn = Instant.parse("2020-12-02T14:48:36Z"),
            redeemedOn = Instant.parse("2020-12-05T14:49:47Z"),
            sentOn = null
        ),
        // 1
        scannedTask(
            scannedOn = Instant.parse("2020-12-02T14:48:37Z"),
            redeemedOn = null,
            sentOn = null
        ),
        // 2
        scannedTask(
            scannedOn = Instant.parse("2020-12-02T14:48:41Z"),
            redeemedOn = null,
            sentOn = null
        ),
        // 3
        scannedTask(
            scannedOn = Instant.parse("2020-12-03T13:40:11Z"),
            redeemedOn = null,
            sentOn = null
        )
    )

// keep in sync with `testSyncedTasks`
val testSyncedTasksOrdered =
    listOf(
        testSyncedTasks[2],
        testSyncedTasks[4],
        testSyncedTasks[3]
    )

// keep in sync with `testScannedTasks`
val testScannedTasksOrdered by lazy {
    listOf(
        testScannedTasks[3],
        testScannedTasks[2],
        testScannedTasks[1]
    )
}

// keep in sync with `testSyncedTasks`
val testRedeemedTasksOrdered =
    listOf(
        testSyncedTasks[5],
        testSyncedTasks[6],
        testSyncedTasks[0],
        testScannedTasks[0],
        testSyncedTasks[1]
    )

val testRedeemedTaskIdsOrdered
    get() =
        testRedeemedTasksOrdered.map {
            when (it) {
                is ScannedTaskData.ScannedTask -> it.taskId
                is SyncedTaskData.SyncedTask -> it.taskId
                else -> error("wrong type")
            }
        }
