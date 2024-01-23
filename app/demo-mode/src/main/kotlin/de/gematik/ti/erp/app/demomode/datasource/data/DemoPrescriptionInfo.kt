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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.NOW
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SHORT_EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SYNCED_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.randomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationDispense
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationPZN
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Quantity
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Ratio
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.FhirTemporal
import java.util.UUID
import kotlin.random.Random

object DemoPrescriptionInfo {

    private val SYNCED_MEDICATION_NAMES = listOf(
        "Ibuprofen 600", "Meloxicam", "Indomethacin", "Celebrex", "Ketoprofen", "Piroxicam", "Etodolac", "Toradol",
        "Aspirin", "Voltaren"
    )

    val SCANNED_MEDICINE_NAMES = listOf(
        "Lopressor", "Tenormin", "Prinivil", "Vasetoc", "Cozaar", "Norvasc", "Plavix", "Nitrostat", "Tambocor",
        "Lanoxin"
    )

    private val DOSAGE = listOf("1-0-1-1", "1-1-1-1", "0-0-0-1", "1-0-1", "0-1-1-0")

    private val STREET_NAMES = listOf(
        "Mühlenweg",
        "Birkenallee",
        "Sonnenstraße",
        "Lindenplatz",
        "Friedensgasse",
        "Bergstraße",
        "Am Rosenhain",
        "Eichenweg",
        "Schlossallee",
        "Marktplatz"
    )

    private val POSTAL_CODES = listOf(
        "10115",
        "20355",
        "30880",
        "40476",
        "50321",
        "60232",
        "70794",
        "81099",
        "90210",
        "10001"
    )

    private val CITY_NAMES = listOf(
        "Berlin",
        "Munich (München)",
        "Hamburg",
        "Cologne (Köln)",
        "Frankfurt",
        "Stuttgart",
        "Düsseldorf",
        "Hannover",
        "Leipzig",
        "Gotha"
    )

    private val FLOORS = listOf(
        "Erdgeschoss",
        "1. Stock",
        "2. Stock",
        "3. Stock",
        "Dachgeschoss"
    )

    private val PHONE_NUMBERS = listOf(
        "+49 123 4567890",
        "+49 234 5678901",
        "+49 345 6789012",
        "+49 456 7890123",
        "+49 567 8901234",
        "+49 678 9012345",
        "+49 789 0123456",
        "+49 890 1234567",
        "+49 901 2345678",
        "+49 012 3456789"
    )

    private val FIRST_NAMES = listOf(
        "Hans",
        "Anna",
        "Max",
        "Sophie",
        "Lukas",
        "Emma",
        "Johann",
        "Maria",
        "Felix",
        "Laura"
    )

    private val NAMES = listOf(
        "Hans Müller",
        "Anna Schmidt",
        "Thomas Wagner",
        "Sabine Fischer",
        "Stefan Becker",
        "Petra Schulz",
        "Andreas Koch",
        "Julia Richter",
        "Martin Bauer",
        "Laura Hoffmann"
    )

    private val MEDICATION_SPECIALITIES = listOf(
        "Fachärztin für Innere Medizin",
        "Facharzt für Orthopädie",
        "Fachärztin für Augenheilkunde",
        "Facharzt für Hals-Nasen-Ohrenheilkunde",
        "Fachärztin für Dermatologie",
        "Facharzt für Neurologie",
        "Fachärztin für Gynäkologie",
        "Facharzt für Urologie",
        "Fachärztin für Pädiatrie",
        "Facharzt für Anästhesiologie"
    )

    private val MEDICAL_PRACTICES = listOf(
        "Praxis Erika Mustermann" to "erika@mustermann.de",
        "Dr. Müller's Praxis" to "dr.mueller@example.com",
        "Gesundheitszentrum Schmidt" to "schmidt@gesundheitszentrum.net",
        "Klinik am See" to "info@klinikamsee.de",
        "Praxisgemeinschaft Weber & Schmidt" to "weber-schmidt@praxis.de",
        "Dr. Wagner & Partner" to "info@wagnerundpartner.com",
        "Gesundheitszentrum Sonnenschein" to "info@gesundheit-sonne.de",
        "Praxis für Allgemeinmedizin Meier" to "meier@praxismed.de",
        "Klinik Rosenpark" to "info@klinikrosenpark.de",
        "Dr. Schmidt's Kinderarztpraxis" to "kinderarzt@drschmidt.de"
    )

    private val DOCTORS_NOTES = listOf(
        "Patient hat grippeähnliche Symptome und sollte sich ausruhen.",
        "Blutdruck im normalen Bereich, Patient sollte regelmäßig Sport treiben.",
        "Anpassung der Medikation notwendig, um den Blutzuckerspiegel zu kontrollieren.",
        "Patient klagt über Kopfschmerzen, möglicherweise aufgrund von Stress.",
        "Regelmäßige Kontrolluntersuchungen werden empfohlen, um den Heilungsverlauf zu überwachen.",
        "Verdacht auf Lebensmittelallergie, Patient sollte Tagebuch über Ernährung führen.",
        "Weitere Tests erforderlich, um die Ursache der Beschwerden zu ermitteln.",
        "Ruhe und ausreichend Schlaf notwendig, um die Genesung zu fördern.",
        "Patient leidet unter Rückenschmerzen, Physiotherapie wird empfohlen.",
        "Erhöhte Cholesterinwerte festgestellt, Anpassung der Ernährung notwendig."
    )

    /**
     * Copied from [de.gematik.ti.erp.app.prescription.repository.KBVCodeMapping.normSizeMapping]
     * in feature module
     */
    private val normSizeMappings = listOf("KA", "KTP", "N1", "N2", "N3", "NB", "Sonstiges")

    /**
     * Copied from [de.gematik.ti.erp.app.prescription.repository.KBVCodeMapping.codeToFormMapping]
     * in feature module
     */
    private val codeToFormMapping = listOf("AEO", "AUB", "TAB", "TKA", "TLE", "VKA", "XHA")

    private val PERFORMERS = listOf(
        "Apotheker",
        "Apothekenhelfer",
        "Arzt",
        "Krankenschwester",
        "Selbst"
    )

    internal const val DEMO_MODE_IDENTIFIER = "1234567890"

    internal val PRACTITIONER = Practitioner(
        name = NAMES.random(),
        qualification = MEDICATION_SPECIALITIES.random(),
        practitionerIdentifier = DEMO_MODE_IDENTIFIER
    )

    private val ADDRESS = SyncedTaskData.Address(
        line1 = STREET_NAMES.random(),
        line2 = FLOORS.random(),
        postalCode = POSTAL_CODES.random(),
        city = CITY_NAMES.random()
    )

    private fun organization(): Organization {
        val item = MEDICAL_PRACTICES.random()
        return Organization(
            name = item.first,
            address = ADDRESS,
            uniqueIdentifier = DEMO_MODE_IDENTIFIER,
            phone = PHONE_NUMBERS.random(),
            mail = item.second
        )
    }

    internal val ORGANIZATION = organization()

    internal val PATIENT = Patient(
        name = "${FIRST_NAMES.random()} Mustermann",
        address = ADDRESS,
        birthdate = null,
        insuranceIdentifier = DEMO_MODE_IDENTIFIER
    )

    private val RATIO = Ratio(
        numerator = Quantity(
            value = listOf("1", "2", "3", "4", "5").random(),
            unit = "oz"
        ),
        denominator = null
    )

    private val MEDICATION = MedicationPZN(
        category = SyncedTaskData.MedicationCategory.values().random(),
        vaccine = Random.nextBoolean(),
        text = SYNCED_MEDICATION_NAMES.random(),
        form = codeToFormMapping.random(),
        lotNumber = DEMO_MODE_IDENTIFIER,
        expirationDate = FhirTemporal.Instant(EXPIRY_DATE),
        uniqueIdentifier = DEMO_MODE_IDENTIFIER,
        normSizeCode = normSizeMappings.random(),
        amount = RATIO
    )

    internal val MEDICATION_DISPENSE = MedicationDispense(
        dispenseId = UUID.randomUUID().toString(),
        patientIdentifier = PATIENT.insuranceIdentifier ?: "",
        medication = MEDICATION,
        wasSubstituted = false,
        dosageInstruction = DOSAGE.random(),
        performer = PERFORMERS.random(),
        whenHandedOver = null
    )

    internal val MEDICATION_REQUEST = MedicationRequest(
        medication = MEDICATION,
        dateOfAccident = null,
        location = CITY_NAMES.random(),
        emergencyFee = Random.nextBoolean(),
        dosageInstruction = DOSAGE.random(),
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
        note = DOCTORS_NOTES.random(),
        substitutionAllowed = Random.nextBoolean()
    )

    internal object DemoScannedPrescription {
        internal val demoScannedTask01 = ScannedTaskData.ScannedTask(
            profileId = demoProfile01.id,
            taskId = "160.000.006.394.157.15",
            index = 1,
            name = SCANNED_MEDICINE_NAMES.random(),
            accessCode = "8cc887c16681517e2db71078f367d4446c156bde743e15c2440722ec0835f406",
            scannedOn = randomTimeToday,
            redeemedOn = null,
            communications = emptyList()
        )
        internal val demoScannedTask02 = ScannedTaskData.ScannedTask(
            profileId = DemoProfileInfo.demoProfile02.id,
            taskId = "160.000.006.386.866.63",
            index = 2,
            name = SCANNED_MEDICINE_NAMES.random(),
            accessCode = "c0967e56ccbcb55ef0851ac9ad3a03dcfbb5ba1934d8d1338290167e348c876f",
            scannedOn = randomTimeToday,
            redeemedOn = null,
            communications = emptyList()
        )
    }

    internal object DemoSyncedPrescription {
        internal val demoSyncedPrescription01 = SyncedTaskData.SyncedTask(/**/
            profileId = demoProfile01.id,
            taskId = "${SYNCED_TASK_PRESET}.1",
            isIncomplete = false,
            pvsIdentifier = DEMO_MODE_IDENTIFIER,
            accessCode = DEMO_MODE_IDENTIFIER,
            lastModified = longerRandomTimeToday,
            organization = ORGANIZATION,
            practitioner = PRACTITIONER,
            patient = PATIENT,
            insuranceInformation = SyncedTaskData.InsuranceInformation(
                name = null,
                status = null
            ),
            expiresOn = EXPIRY_DATE,
            acceptUntil = SHORT_EXPIRY_DATE,
            authoredOn = NOW,
            status = SyncedTaskData.TaskStatus.Ready,
            medicationRequest = MEDICATION_REQUEST,
            medicationDispenses = listOf(MEDICATION_DISPENSE),
            communications = emptyList(),
            failureToReport = ""
        )

        internal fun syncedTask(
            profileIdentifier: ProfileIdentifier,
            status: SyncedTaskData.TaskStatus = SyncedTaskData.TaskStatus.Ready,
            index: Int
        ) = SyncedTaskData.SyncedTask(
            profileId = profileIdentifier,
            taskId = "$SYNCED_TASK_PRESET.$index",
            isIncomplete = false,
            pvsIdentifier = DEMO_MODE_IDENTIFIER,
            accessCode = DEMO_MODE_IDENTIFIER,
            lastModified = longerRandomTimeToday,
            organization = ORGANIZATION,
            practitioner = PRACTITIONER,
            patient = PATIENT,
            insuranceInformation = SyncedTaskData.InsuranceInformation(
                name = null,
                status = null
            ),
            expiresOn = EXPIRY_DATE,
            acceptUntil = SHORT_EXPIRY_DATE,
            authoredOn = NOW,
            status = status,
            medicationRequest = MEDICATION_REQUEST,
            medicationDispenses = listOf(MEDICATION_DISPENSE),
            communications = emptyList(),
            failureToReport = ""
        )
    }
}
