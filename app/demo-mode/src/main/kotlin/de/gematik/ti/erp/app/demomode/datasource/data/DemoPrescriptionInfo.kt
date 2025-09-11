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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.DIGA_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.DIRECT_ASSIGNMENT_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.NOW
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SHORT_EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.START_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SYNCED_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.randomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.RequestIntent
import de.gematik.ti.erp.app.fhir.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
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
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.random.Random

object DemoPrescriptionInfo {

    private val BOOLEAN = listOf(true, false)

    private val SYNCED_MEDICATION_NAMES = setOf(
        "Placebex", "Ibupretend", "Fauxprofen", "Chilladrin", "SniffleSnuff", "Yawnitol", "Laughprofen", "Snorezine",
        "Zzzquilish", "Hypernex", "Gigglomycin", "Napril", "Procrastifen", "Mildramol", "Slightofen", "Drowsinol",
        "Blinkofen", "Mehprofen", "Kindaproxin", "Driftazine", "Daydreamex", "Lowkeymax", "Forgetix", "Awkwardol",
        "Oopsazepam", "Shrugonex", "Dramaquill", "Lazitone", "Yawndol", "Apologex", "Confusitol", "Grumpex",
        "Overthinkol", "Zoomedol", "Couchacillin", "Unmotivon", "Reactorex", "Stresstonin", "Underthinkol",
        "Panickinex", "Petmycatin", "Scrolladrine", "Complainex", "Doomazon", "Snackitrol", "Notonightin",
        "Internexol", "Snackodone", "Zebraline", "Decaffidrin"
    )

    val SCANNED_MEDICINE_NAMES = listOf(
        "Pill-o-tron 5000", "Mood Swinger", "HeartBeats-a-Lot", "PlaceboMax", "NoPainAllGain",
        "GrumpAway", "Chillaxin", "WakeMeUpNow", "NapZap", "DoctorFeelGood"
    )

    private val DOSAGE = listOf("1-0-1-1", "1-1-1-1", "0-0-0-1", "1-0-1", "0-1-1-0")

    private val STREET_NAMES = listOf(
        "Wurstweg", // Sausage Way 🌭
        "Kaffeeschlürfgasse", // Coffee Slurp Lane ☕
        "Schnarchplatz", // Snore Square 😴
        "Bratwurstallee", // Sausage Blvd 🔥
        "Faulenzerstraße", // Lazybones Street 🛋️
        "Dönerwinkel", // Döner Corner 🌯
        "Bierdeckelring", // Beer Coaster Ring 🍺
        "Schnitzelstraße", // Schnitzel Street 🍽️
        "Quasselgasse", // Chatter Alley 🗣️
        "Möpseweg", // Pugs Way 🐶
        "NichtHierweg", // "NotHere Way" 🚷
        "Verlorenesockenweg", // Lost Sock Way 🧦
        "Butterbrotstraße", // Sandwich Street 🧈🍞
        "Lachflashgasse", // Giggle Burst Lane 😂
        "Gurkenplatz", // Cucumber Square 🥒
        "Zahnlückenring", // Tooth Gap Circle 😁
        "Keksweg", // Cookie Lane 🍪
        "Mumpitzallee", // Nonsense Ave 🌀
        "Katzenjammerweg", // Cat Wail Way 😾
        "Niesattackengasse" // Sneezing Fit Alley 🤧
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

    val CITY_NAMES = listOf(
        "Schnitzelburg", // City of schnitzel pride 🍖
        "Wursthausen", // Sausage capital 🌭
        "Bratkartoffelheim", // Pan-fried potato central 🥔
        "Bierdorf", // Beer Town 🍺
        "Kaffekranzingen", // Coffee & gossip central ☕
        "Schnarchstadt", // Sleepy town 😴
        "Oberunterhinterdorf", // Too many directions to find 🧭
        "Flauschingen", // Fluffy feels town 🧸
        "Langweiligen", // Boringville 😐
        "Müsliberg", // Granola hipster heaven 🌾
        "Gurkental", // Cucumber Valley 🥒
        "Pups am See", // Giggle alert 💨 + 🏞️
        "Knödlingen", // Dumplingville 🥟
        "Katzenfurt", // Cat River City 🐱
        "Lachstadt", // Laughter City 😄
        "Dönerdorf", // Where every street has a kebab 🌯
        "Neuschwaflingen", // Fake cousin of Neuschwanstein 🏰
        "Unbekanntstadt", // Unknownville ❓
        "Sauerkraut am Rhein", // You already smell it 🇩🇪
        "Technobach" // Raves & baroque music 🎧🎼
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
        "Patient zeigt typische Symptome eines Prototypen. Weitere Entwicklung empfohlen.",
        "Diagnose: leichte Datenmüdigkeit. Bitte regelmäßig cache leeren.",
        "Symptome deuten auf akute Demomüdigkeit hin. App einfach mal neu starten.",
        "Keine Auffälligkeiten festgestellt – außer einer Vorliebe für Lorem Ipsum.",
        "Patient berichtet über Kopfweh – möglicherweise durch zu viele Meetings.",
        "Blutdruck stabil. Stresslevel steigt nur bei Jenkins-Fehlschlägen.",
        "Empfehlung: Weniger Scrollen, mehr frische Luft. (Auch für den Entwickler!)",
        "Ernährungsumstellung empfohlen: weniger Bugs, mehr Features.",
        "Medikation gut eingestellt. Bitte keine UI-Änderungen mehr vor dem Release.",
        "Patient im stabilen Zustand, solange keine Netzwerkverbindung unterbrochen wird.",
        "Rückenschmerzen wahrscheinlich durch stundenlanges Mock-Daten eingeben.",
        "Nächste Kontrolle bei Release-Kandidaten-Status oder spontaner Regression.",
        "Patient leidet unter akuter '404 – Motivation Not Found'. Behandlung läuft.",
        "Verdacht auf Feature Fatigue. Bitte keine neuen Anforderungen diese Woche.",
        "Testnote für Demo-Zwecke. Keine echten medizinischen Inhalte enthalten."
    )

    /**
     * Copied from [KBVCodeMapping.normSizeMapping]
     * in feature module
     */
    private val normSizeMappings = listOf("KA", "KTP", "N1", "N2", "N3", "NB", "Sonstiges")

    /**
     * Copied from [KBVCodeMapping.codeToFormMapping]
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
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
        packaging = null
    )

    private val COVERAGE_TYPE = SyncedTaskData.CoverageType.entries
        .toTypedArray().random()

    private fun medication(index: Int) = Medication(
        category = SyncedTaskData.MedicationCategory.entries.toTypedArray().random(),
        vaccine = Random.nextBoolean(),
        text = when (index) {
            0 -> "\uD83C\uDDEA\uD83C\uDDFA EU Medikament Prescription"
            30 -> "\uD83C\uDDEA\uD83C\uDDFA EU Aspirin 500mg"
            else -> SYNCED_MEDICATION_NAMES.elementAtOrElse(index) { SYNCED_MEDICATION_NAMES.random() }
        },
        form = codeToFormMapping.random(),
        lotNumber = DEMO_MODE_IDENTIFIER,
        expirationDate = FhirTemporal.Instant(EXPIRY_DATE),
        identifier = SyncedTaskData.Identifier(DEMO_MODE_IDENTIFIER),
        normSizeCode = normSizeMappings.random(),
        amount = RATIO,
        ingredientMedications = emptyList(),
        ingredients = emptyList(),
        manufacturingInstructions = null,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
        packaging = null
    )

    private fun medicationDispense(
        isCompleted: Boolean,
        isDeviceRequest: Boolean
    ) = MedicationDispense(
        dispenseId = UUID.randomUUID().toString(),
        patientIdentifier = PATIENT.insuranceIdentifier ?: "",
        medication = MEDICATION,
        wasSubstituted = BOOLEAN.random(),
        dosageInstruction = DOSAGE.random(),
        performer = PERFORMERS.random(),
        whenHandedOver = null,
        deviceRequest = if (isDeviceRequest) deviceRequestDispense(isCompleted) else null
    )

    @Suppress("ktlint:max-line-length", "MaxLineLength")
    private fun deviceRequestDispense(isCompleted: Boolean) = FhirDispenseDeviceRequestErpModel(
        deepLink = "intent://maps.google.com/maps?q=Friedrichstraße+136+Berlin+Germany#Intent;scheme=https;package=com.google.android.apps.maps;S.browser_fallback_url=https://maps.google.com?q=Friedrichstraße+136+Berlin+Germany;end",
        redeemCode = FUNNY_REDEEM_CODES.random(),
        declineCode = null,
        note = null,
        referencePzn = "420",
        display = "Super Legit DiGA App™️ v9000",
        status = if (isCompleted) "completed" else "reject",
        modifiedDate = Instant.parse(input = "2024-08-01T10:00:00Z").asFhirTemporal()
    )

    internal val INSURANCE_INFORMATION = SyncedTaskData.InsuranceInformation(
        name = null,
        status = null,
        coverageType = COVERAGE_TYPE
    )

    private val FUNNY_REDEEM_CODES = listOf(
        "TRUST_ME_BRO",
        "NOT_A_SCAM",
        "1234-5678-LOL",
        "FREECAKE_INSIDE",
        "CERTIFIED_FAKE",
        "USE_AT_OWN_RISK",
        "I_KNOW_A_GUY",
        "PROB_NOT_EXPIRED",
        "ACTUALLY_WORKS",
        "NO_REFUNDS",
        "REDEEM_AND_REGRET",
        "DONT_TELL_SUPPORT",
        "YOLO2025",
        "ITS_FINE_PROBABLY",
        "TOTALLY_LEGAL",
        "SECRET_SAUCE",
        "REDEEM_YOUR_FAITH",
        "VALID_UNTIL_YESTERDAY",
        "FAKECODE123",
        "IM_NOT_A_ROBOT"
    )

    internal fun medicationRequest(isDeviceRequest: Boolean, index: Int) = MedicationRequest(
        medication = if (!isDeviceRequest) medication(index) else null,
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

    fun demoDiga(
        index: Int?,
        appName: String?
    ) = FhirTaskKbvDeviceRequestErpModel(
        id = "199",
        intent = RequestIntent.Proposal,
        status = "active",
        pzn = "123457590456",
        appName = appName ?: FUNNY_APP_NAMES.random(),
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.WorkAccident,
            date = FhirTemporal.LocalDate(LocalDate.parse("2025-03-28")),
            location = CITY_NAMES.random()
        ),
        userActionState = index?.let { listOfDeviceRequestStatus[it] },
        isSelfUse = false,
        authoredOn = FhirTemporal.Instant(START_DATE),
        isNew = listOf(true, false).random(),
        isArchived = false
    )

    private val FUNNY_APP_NAMES = listOf(
        "Demo DiGa App",
        "TotallyNotSpyware",
        "MediLOL",
        "Prescription Impossible",
        "Appy McAppface",
        "Pill It Up!",
        "Tap That Tablet",
        "Dr. Feelgood's Assistant",
        "Placebo Pro",
        "CureOS",
        "Heal Yeah!",
        "Med-Zilla",
        "The Daily Dose",
        "404 Symptoms Found",
        "Take This App And Call Me",
        "SickNote Simulator",
        "DigiDripp",
        "QuackTrack",
        "PainAway.exe",
        "Sniffle Solutions™️"
    )

    private val listOfDeviceRequestStatus: List<DigaStatus> = listOf(
        DigaStatus.Ready,
        DigaStatus.InProgress(Instant.parse("2024-07-01T10:00:00Z")),
        DigaStatus.CompletedSuccessfully,
        DigaStatus.CompletedWithRejection(Instant.parse("2024-08-01T10:00:00Z")),
        DigaStatus.DownloadDigaApp,
        DigaStatus.OpenAppWithRedeemCode,
        DigaStatus.ReadyForSelfArchiveDiga,
        DigaStatus.SelfArchiveDiga
    )

    internal object DemoSyncedPrescription {
        internal fun syncedTask(
            profileIdentifier: ProfileIdentifier,
            status: SyncedTaskData.TaskStatus = SyncedTaskData.TaskStatus.Ready,
            isDirectAssignment: Boolean = false,
            isDeviceRequest: Boolean = false,
            isDeviceRequestCompleted: Boolean = false,
            deviceRequestStatusIndex: Int? = null,
            medicationNamesIndex: Int,
            appName: String? = null,
            isEuRedeemable: Boolean = false
        ): SyncedTaskData.SyncedTask {
            val taskId =
                when {
                    isDirectAssignment -> "$DIRECT_ASSIGNMENT_TASK_PRESET.$medicationNamesIndex"
                    isDeviceRequest -> "$DIGA_TASK_PRESET.$medicationNamesIndex"
                    else -> "$SYNCED_TASK_PRESET.$medicationNamesIndex"
                }
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
                expiresOn = if (medicationNamesIndex == 30) Clock.System.now() else EXPIRY_DATE,
                acceptUntil = SHORT_EXPIRY_DATE,
                authoredOn = NOW,
                status = status,
                medicationRequest = medicationRequest(isDeviceRequest, medicationNamesIndex),
                lastMedicationDispense = null,
                medicationDispenses = listOf(
                    medicationDispense(
                        isCompleted = isDeviceRequestCompleted,
                        isDeviceRequest = isDeviceRequest
                    )
                ),
                communications = emptyList(),
                failureToReport = "",
                deviceRequest = if (isDeviceRequest) demoDiga(deviceRequestStatusIndex, appName) else null,
                isEuRedeemable = isEuRedeemable
            )
        }
    }
}
