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

package de.gematik.ti.erp.app.datasource.data

import de.gematik.ti.erp.app.datasource.data.MockConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.datasource.data.MockConstants.NOW
import de.gematik.ti.erp.app.datasource.data.MockConstants.SHORT_EXPIRY_DATE
import de.gematik.ti.erp.app.datasource.data.MockConstants.SYNCED_TASK_PRESET
import de.gematik.ti.erp.app.datasource.data.MockConstants.fixedTime
import de.gematik.ti.erp.app.datasource.data.MockConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.datasource.data.MockProfileInfo.mockProfile01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.RequestIntent
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationDispense
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.util.UUID

object MockPrescriptionInfo {

    private val BOOLEAN = listOf(true, false)

    private const val SYNCED_MEDICATION_NAMES =
        "Ibuprofen 600"

    private const val SCANNED_MEDICINE_NAMES = "Lopressor"

    private const val DOSAGE = "1-0-1-1"

    private const val STREET_NAMES = "Marktplatz"

    private const val POSTAL_CODES = "10115"

    private const val CITY_NAMES = "Berlin"

    private const val FLOORS = "1. Stock"

    private const val PHONE_NUMBERS = "+49 123 4567890"

    private const val FIRST_NAMES = "Hans"

    private const val NAMES = "Hans Müller"

    private const val MEDICATION_SPECIALITIES = "Fachärztin für Innere Medizin"

    private val MEDICAL_PRACTICES = "Praxis Erika Mustermann" to "erika@mustermann.de"

    private const val DOCTORS_NOTES = "Patient hat grippeähnliche Symptome und sollte sich ausruhen."

    private const val normSizeMappings = "KA"

    private const val codeToFormMapping = "AEO"

    private const val PERFORMERS = "Apotheker"

    internal const val MOCK_IDENTIFIER = "1234567890"

    internal val PRACTITIONER = Practitioner(
        name = NAMES,
        qualification = MEDICATION_SPECIALITIES,
        practitionerIdentifier = MOCK_IDENTIFIER
    )

    private val ADDRESS = SyncedTaskData.Address(
        line1 = STREET_NAMES,
        line2 = FLOORS,
        postalCode = POSTAL_CODES,
        city = CITY_NAMES
    )

    private fun organization(): Organization {
        val item = MEDICAL_PRACTICES
        return Organization(
            name = item.first,
            address = ADDRESS,
            uniqueIdentifier = MOCK_IDENTIFIER,
            phone = PHONE_NUMBERS,
            mail = item.second
        )
    }

    internal val ORGANIZATION = organization()

    internal val PATIENT = Patient(
        name = "$FIRST_NAMES Mustermann",
        address = ADDRESS,
        birthdate = null,
        insuranceIdentifier = MOCK_IDENTIFIER
    )

    private val RATIO = Ratio(
        numerator = Quantity(
            value = "1",
            unit = "oz"
        ),
        denominator = null
    )

    private val MEDICATION = SyncedTaskData.Medication(
        category = SyncedTaskData.MedicationCategory.entries[0],
        vaccine = true,
        text = SYNCED_MEDICATION_NAMES,
        form = codeToFormMapping,
        lotNumber = MOCK_IDENTIFIER,
        expirationDate = FhirTemporal.Instant(EXPIRY_DATE),
        identifier = SyncedTaskData.Identifier(
            pzn = MOCK_IDENTIFIER
        ),
        normSizeCode = normSizeMappings,
        amount = RATIO,
        manufacturingInstructions = "Nehmen Sie die Tabletten mit Wasser ein.",
        packaging = null,
        ingredientMedications = emptyList(),
        ingredients = emptyList()
    )

    internal val MEDICATION_DISPENSE = MedicationDispense(
        dispenseId = UUID.randomUUID().toString(),
        patientIdentifier = PATIENT.insuranceIdentifier ?: "",
        medication = MEDICATION,
        wasSubstituted = true,
        dosageInstruction = DOSAGE,
        performer = PERFORMERS,
        whenHandedOver = null,
        deviceRequest = null
    )

    internal var MEDICATION_REQUEST = MedicationRequest(
        medication = MEDICATION,
        dateOfAccident = null,
        location = CITY_NAMES,
        emergencyFee = true,
        dosageInstruction = DOSAGE,
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
        note = DOCTORS_NOTES,
        substitutionAllowed = true
    )

    internal object MockScannedPrescription {
        internal val mockScannedTask01 = ScannedTaskData.ScannedTask(
            profileId = mockProfile01.id,
            taskId = "160.000.006.394.157.15",
            index = 0,
            name = SCANNED_MEDICINE_NAMES,
            accessCode = "8cc887c16681517e2db71078f367d4446c156bde743e15c2440722ec0835f406",
            scannedOn = fixedTime,
            redeemedOn = null,
            communications = emptyList()
        )
        internal val mockScannedTask02 = ScannedTaskData.ScannedTask(
            profileId = mockProfile01.id,
            taskId = "160.000.006.386.866.63",
            index = 1,
            name = SCANNED_MEDICINE_NAMES,
            accessCode = "c0967e56ccbcb55ef0851ac9ad3a03dcfbb5ba1934d8d1338290167e348c876f",
            scannedOn = fixedTime,
            redeemedOn = null,
            communications = emptyList()
        )
    }
    internal val DEMO_DIGA = FhirTaskKbvDeviceRequestErpModel(
        id = "1",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "123457590",
        appName = "diga app",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.WorkAccident,
            date = FhirTemporal.LocalDate(LocalDate.parse("2025-03-28")),
            location = DemoPrescriptionInfo.CITY_NAMES.random()
        ),
        isSelfUse = true,
        authoredOn = FhirTemporal.Instant(Instant.DISTANT_PAST),
        isNew = false
    )
    internal object MockSyncedPrescription {
        internal fun syncedTask(
            profileIdentifier: ProfileIdentifier,
            status: SyncedTaskData.TaskStatus = SyncedTaskData.TaskStatus.Ready,
            index: Int
        ) = SyncedTaskData.SyncedTask(
            profileId = profileIdentifier,
            taskId = "$SYNCED_TASK_PRESET.$index",
            isIncomplete = false,
            pvsIdentifier = MOCK_IDENTIFIER,
            accessCode = MOCK_IDENTIFIER,
            lastModified = longerRandomTimeToday,
            organization = ORGANIZATION,
            practitioner = PRACTITIONER,
            patient = PATIENT,
            insuranceInformation = SyncedTaskData.InsuranceInformation(
                name = null,
                status = null,
                coverageType = SyncedTaskData.CoverageType.GKV
            ),
            expiresOn = EXPIRY_DATE,
            acceptUntil = SHORT_EXPIRY_DATE,
            authoredOn = NOW,
            status = status,
            medicationRequest = MEDICATION_REQUEST.copy(
                substitutionAllowed = BOOLEAN[index]
            ), // Making sure the substitutionAllowed is different for each task
            medicationDispenses = listOf(MEDICATION_DISPENSE),
            communications = emptyList(),
            lastMedicationDispense = null,
            failureToReport = "",
            deviceRequest = DEMO_DIGA
        )
    }
}
