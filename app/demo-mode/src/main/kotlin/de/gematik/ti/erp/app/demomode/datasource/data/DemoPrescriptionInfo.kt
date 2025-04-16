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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.DIRECT_ASSIGNMENT_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.NOW
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SHORT_EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SYNCED_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.randomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Medication
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationDispense
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.FhirTemporal
import java.util.UUID
import kotlin.random.Random

object DemoPrescriptionInfo {

    private val BOOLEAN = listOf(true, false)

    private val SYNCED_MEDICATION_NAMES = setOf(
        "Ibuprofen 600", "Meloxicam", "Indomethacin", "Celebrex", "Ketoprofen", "Piroxicam", "Etodolac", "Toradol",
        "Aspirin", "Voltaren", "Naproxen", "Mobic", "Aleve", "Motrin", "Advil", "Relafen", "Feldene", "Daypro",
        "Clinoril", "Ansaid", "Orudis", "Dolobid", "Tolectin", "Lodine", "Nalfon", "Indocin", "Arthrotec", "Vimovo",
        "Cataflam", "Pennsaid", "Zipsor", "Voltaren Gel"
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

    private val MEDICATION = Medication(
        category = SyncedTaskData.MedicationCategory.entries.toTypedArray().random(),
        vaccine = Random.nextBoolean(),
        text = SYNCED_MEDICATION_NAMES.random(),
        form = codeToFormMapping.random(),
        lotNumber = DEMO_MODE_IDENTIFIER,
        expirationDate = FhirTemporal.Instant(EXPIRY_DATE),
        identifier = SyncedTaskData.Identifier(DEMO_MODE_IDENTIFIER),
        normSizeCode = normSizeMappings.random(),
        amount = RATIO,
        ingredientMedications = emptyList(),
        ingredients = emptyList(),
        manufacturingInstructions = null,
        packaging = null
    )

    private val COVERAGE_TYPE = SyncedTaskData.CoverageType.entries
        .toTypedArray().random()

    private fun medication(index: Int) = Medication(
        category = SyncedTaskData.MedicationCategory.entries.toTypedArray().random(),
        vaccine = Random.nextBoolean(),
        text = SYNCED_MEDICATION_NAMES.elementAtOrElse(index) { SYNCED_MEDICATION_NAMES.random() },
        form = codeToFormMapping.random(),
        lotNumber = DEMO_MODE_IDENTIFIER,
        expirationDate = de.gematik.ti.erp.app.utils.FhirTemporal.Instant(EXPIRY_DATE),
        identifier = SyncedTaskData.Identifier(DEMO_MODE_IDENTIFIER),
        normSizeCode = normSizeMappings.random(),
        amount = RATIO,
        ingredientMedications = emptyList(),
        ingredients = emptyList(),
        manufacturingInstructions = null,
        packaging = null
    )

    internal val MEDICATION_DISPENSE = MedicationDispense(
        dispenseId = UUID.randomUUID().toString(),
        patientIdentifier = PATIENT.insuranceIdentifier ?: "",
        medication = MEDICATION,
        wasSubstituted = BOOLEAN.random(),
        dosageInstruction = DOSAGE.random(),
        performer = PERFORMERS.random(),
        whenHandedOver = null
    )

    internal val INSURANCE_INFORMATION = SyncedTaskData.InsuranceInformation(
        name = null,
        status = null,
        coverageType = COVERAGE_TYPE
    )

    internal fun medicationRequest(index: Int) = MedicationRequest(
        medication = medication(index),
        dateOfAccident = null,
        location = CITY_NAMES.random(),
        emergencyFee = BOOLEAN.random(),
        dosageInstruction = DOSAGE.random(),
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
        note = DOCTORS_NOTES.random(),
        substitutionAllowed = BOOLEAN.random()
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
        internal fun syncedTask(
            profileIdentifier: ProfileIdentifier,
            status: SyncedTaskData.TaskStatus = SyncedTaskData.TaskStatus.Ready,
            isDirectAssignment: Boolean = false,
            index: Int
        ): SyncedTaskData.SyncedTask {
            val taskId =
                if (isDirectAssignment) "$DIRECT_ASSIGNMENT_TASK_PRESET.$index" else "$SYNCED_TASK_PRESET.$index"
            return SyncedTaskData.SyncedTask(
                profileId = profileIdentifier,
                taskId = taskId,
                isIncomplete = false, // making this true makes the prescription defective
                pvsIdentifier = DEMO_MODE_IDENTIFIER,
                accessCode = DEMO_MODE_IDENTIFIER,
                lastModified = longerRandomTimeToday,
                organization = ORGANIZATION,
                practitioner = PRACTITIONER,
                patient = PATIENT,
                insuranceInformation = INSURANCE_INFORMATION,
                expiresOn = EXPIRY_DATE,
                acceptUntil = SHORT_EXPIRY_DATE,
                authoredOn = NOW,
                status = status,
                medicationRequest = medicationRequest(index),
                lastMedicationDispense = null,
                medicationDispenses = listOf(MEDICATION_DISPENSE),
                communications = emptyList(),
                failureToReport = ""
            )
        }
    }
}
