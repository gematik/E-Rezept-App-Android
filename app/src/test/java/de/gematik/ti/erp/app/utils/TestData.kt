/*
 * Copyright (c) 2021 gematik GmbH
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

import ca.uhn.fhir.context.FhirContext
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.usecase.createMatrixCode
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.OffsetDateTime
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MedicationDispense

const val ASSET_BASE_PATH = "../app/src/test/assets/"

private val fhirContext by lazy {
    FhirContext.forR4()
}

val TEST_TASK_GROUP_SYNCED = arrayOf(
    "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
    "Task/a90e60e3-75a2-458d-a027-1539fa612f83",
    "Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e",
    "Task/2910233f-0ea2-46b7-b174-240a6240de3a"
)
val TEST_TASK_GROUP_SCANNED = arrayOf(
    "Task/376ed5ef-7f9a-40de-baf8-593de2417124",
    "Task/30aa54cc-a541-4163-811b-0bed57ce7230"
)

fun testTasks() = listOf(
    Task(
        taskId = "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
        accessCode = "71f62e55a662456195049c59f5c19eb371f62e55a662456195049c59f5c19eb3",
        rawKBVBundle = "{}".toByteArray(),
    ),
    Task(
        taskId = "Task/a90e60e3-75a2-458d-a027-1539fa612f83",
        accessCode = "2f5a441e77fc44178f4eea2e6d19a23a2f5a441e77fc44178f4eea2e6d19a23a",
        rawKBVBundle = "{}".toByteArray(),
    ),
    Task(
        taskId = "Task/376ed5ef-7f9a-40de-baf8-593de2417124",
        accessCode = "bcc13212c7674cb3bc465a78efe0992ebcc13212c7674cb3bc465a78efe0992e",
        scannedOn = OffsetDateTime.parse("2020-12-02T14:48:41+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
        nrInScanSession = 7,
        scanSessionName = null,

    ),
    Task(
        taskId = "Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e",
        accessCode = "3ea8dc08e5aa4693825437cf73e6d0333ea8dc08e5aa4693825437cf73e6d033",
        rawKBVBundle = "{}".toByteArray(),
    ),
    Task(
        taskId = "Task/30aa54cc-a541-4163-811b-0bed57ce7230",
        accessCode = "600dd956fab74e3fb842d5afaee20401600dd956fab74e3fb842d5afaee20401",
        scannedOn = OffsetDateTime.parse("2020-12-03T13:40:11+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-03T13:42:41+00:00"),
        nrInScanSession = 1,
        scanSessionName = "Some Other Name",
    ),
    Task(
        taskId = "Task/2910233f-0ea2-46b7-b174-240a6240de3a",
        accessCode = "82de8475f352482dbd602972c6024c6a82de8475f352482dbd602972c6024c6a",
        rawKBVBundle = "{}".toByteArray(),
    ),
)

const val TEST_SYNCED_TASK_GROUPS = 3
val TEST_SYNCED_TASK_GROUP_1 = arrayOf("Task/2910233f-0ea2-46b7-b174-240a6240de3a")
val TEST_SYNCED_TASK_GROUP_2 = arrayOf("Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e")
val TEST_SYNCED_TASK_GROUP_3 = arrayOf(
    "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
    "Task/a90e60e3-75a2-458d-a027-1539fa612f83"
)

fun testSyncedTasks() = listOf(
    Task(
        taskId = "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
        accessCode = "71f62e55a662456195049c59f5c19eb371f62e55a662456195049c59f5c19eb3",
        organization = "Praxis Glücklicher gehts nicht",
        medicationText = "Schokolade",
        expiresOn = null,
        authoredOn = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
    ),
    Task(
        taskId = "Task/a90e60e3-75a2-458d-a027-1539fa612f83",
        accessCode = "2f5a441e77fc44178f4eea2e6d19a23a2f5a441e77fc44178f4eea2e6d19a23a",
        organization = "Praxis Glücklicher gehts nicht",
        medicationText = "Bonbons",
        expiresOn = null,
        authoredOn = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
    ),
    Task(
        taskId = "Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e",
        accessCode = "3ea8dc08e5aa4693825437cf73e6d0333ea8dc08e5aa4693825437cf73e6d033",
        organization = "Praxis Glücklicher gehts nicht",
        medicationText = "Gummibärchen",
        expiresOn = null,
        authoredOn = OffsetDateTime.parse("2020-12-05T09:49:46+00:00"),
    ),
    Task(
        taskId = "Task/2910233f-0ea2-46b7-b174-240a6240de3a",
        accessCode = "82de8475f352482dbd602972c6024c6a82de8475f352482dbd602972c6024c6a",
        organization = "MVZ Haus der vielen Ärzte",
        medicationText = "Viel zu viel",
        expiresOn = LocalDate.parse("2021-04-01"),
        authoredOn = OffsetDateTime.parse("2020-12-20T09:49:46+00:00"),
    ),
)

const val TEST_SCANNED_TASK_GROUPS = 2
val TEST_SCANNED_TASK_GROUP_1 = arrayOf("Task/2910233f-0ea2-46b7-b174-240a6240de3a")
val TEST_SCANNED_TASK_GROUP_2 = arrayOf(
    "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
    "Task/a90e60e3-75a2-458d-a027-1539fa612f83",
    "Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e"
)

fun testScannedTasks() = listOf(
    Task(
        taskId = "Task/5c19492e-1dd2-11b2-803a-63bf44e44fb8",
        accessCode = "71f62e55a662456195049c59f5c19eb371f62e55a662456195049c59f5c19eb3",
        scannedOn = OffsetDateTime.parse("2020-12-02T14:48:36+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
        nrInScanSession = 0,
        scanSessionName = "Foo",
    ),
    Task(
        taskId = "Task/a90e60e3-75a2-458d-a027-1539fa612f83",
        accessCode = "2f5a441e77fc44178f4eea2e6d19a23a2f5a441e77fc44178f4eea2e6d19a23a",
        scannedOn = OffsetDateTime.parse("2020-12-02T14:48:37+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
        nrInScanSession = 1,
        scanSessionName = null,
    ),
    Task(
        taskId = "Task/1aeea131-651a-4229-8b16-9bdc73dbdb6e",
        accessCode = "3ea8dc08e5aa4693825437cf73e6d0333ea8dc08e5aa4693825437cf73e6d033",
        scannedOn = OffsetDateTime.parse("2020-12-02T14:48:41+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-02T14:49:46+00:00"),
        nrInScanSession = 2,
        scanSessionName = null,
    ),
    Task(
        taskId = "Task/2910233f-0ea2-46b7-b174-240a6240de3a",
        accessCode = "82de8475f352482dbd602972c6024c6a82de8475f352482dbd602972c6024c6a",
        scannedOn = OffsetDateTime.parse("2020-12-03T13:40:11+00:00"),
        scanSessionEnd = OffsetDateTime.parse("2020-12-03T13:42:41+00:00"),
        nrInScanSession = 0,
        scanSessionName = "Some Name",
    ),
)

fun detailPrescriptionScanned(scannedOn: OffsetDateTime = OffsetDateTime.now()) =
    UIPrescriptionDetailScanned(
        taskId = "4711",
        redeemedOn = OffsetDateTime.now(),
        "accessCode",
        testMatrix(),
        1,
        scannedOn,
        unRedeemMorePossible = true
    )

fun testMatrix() = createMatrixCode("Task/$4711/\$accept?ac=accessCode")

fun challenge(challenge: String): Challenge? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(Challenge::class.java)
    return adapter.fromJson(challenge)
}

fun tokenResponse(tokenResponse: String): TokenResponse? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(TokenResponse::class.java)
    return adapter.fromJson(tokenResponse)
}

//
// fun testUIData() = UIPrescription("foo", ZonedDateTime.now())
//
// fun testPatient(): List<Patient> {
//    val patient = Patient()
//    patient.addName(HumanName().setFamily("foo"))
//    patient.birthDate = Date()
//    return listOf(patient)
// }
//
// fun testUIPrescriptions() = listOf(UIPrescription("foo", ZonedDateTime.now()))
// fun testMedications() = listOf(
//    testMedication(),
//    testMedication()
// )
//
// fun testScanTasks() = listOf(
//    ScanTask(
//        "id",
//        "accessCode",
//        LocalDateTime.now(),
//        LocalDateTime.now(),
//        null
//    )
// )
//
// fun testMedication(): Medication =
//    Medication(
//        id = "42",
//        OffsetDateTime.now(),
//        OffsetDateTime.now(),
//        "someNote",
//        "prescriptionId",
//        "medicationText",
//        "taskId",
//        "practitionerId",
//        "patientId"
//    )
//
// fun getPractitionerMedications(): List<PractitionerAndMedications> {
//    val groupSize = 3
//    val prescriptionGroups = ArrayList<PractitionerAndMedications>(groupSize)
//    for (group in 0 until groupSize) {
//        val variousCountOfPrescriptions = (Math.random() * 6).toInt() + 1
//        val medications =
//            ArrayList<Medication>(variousCountOfPrescriptions)
//        for (i in 0 until variousCountOfPrescriptions) {
//            medications.add(
//                Medication(
//                    "some Id",
//                    OffsetDateTime.now().plusDays(-((group + 1) + group * group).toLong()),
//                    OffsetDateTime.now().plusDays(i.toLong()),
//                    null,
//                    "some id",
//                    "Ganz tolles Medikament $i",
//                    (Math.random() * 1000).toString(),
//                    "some id",
//                    "some id"
//                )
//            )
//        }
//        prescriptionGroups.add(
//            PractitionerAndMedications(
//                Practitioner(
//                    "some id",
//                    "Hans-Peter",
//                    "von Glücklich am See $group",
//                    "Dr. Dr. med",
//                ),
//                medications
//            )
//        )
//    }
//    return prescriptionGroups
// }

fun getBundleFromAssetFileName(filename: String): Bundle {
    val parser = fhirContext.newJsonParser()
    val jsonAsString = readJsonFile(filename)
    return parser.parseResource(jsonAsString) as Bundle
}

fun getMedicationDispenseFromAssetFileName(filename: String): MedicationDispense {
    val parser = fhirContext.newJsonParser()
    val jsonAsString = readJsonFile(filename)
    return parser.parseResource(jsonAsString) as MedicationDispense
}

fun testBundle(): Bundle {
    return getBundleFromAssetFileName("task_bundle.json")
}

fun testCommunicationBundle(): Bundle {
    return getBundleFromAssetFileName("communication_bundle.json")
}

fun testSingleKBVBundle(): Bundle {
    return getBundleFromAssetFileName("kbv_bundle.json")
}

fun taskWithoutKBVBundle(): Bundle {
    return getBundleFromAssetFileName("task_without_kbv_bundle.json")
}

fun taskWithBundle(): Bundle {
    return getBundleFromAssetFileName("task_with_bundle_response.json")
}

fun allAuditEvents(): Bundle {
    return getBundleFromAssetFileName("audit_event_dev.json")
}

fun testPharmacySearchBundle(): Bundle {
    val parser = fhirContext.newJsonParser()
    val jsonAsString = readJsonFile("pharmacy_result_bundle.json")
    return parser.parseResource(jsonAsString) as Bundle
}

fun emptyTestBundle(): Bundle {
    val parser = fhirContext.newJsonParser()
    val jsonAsString = "{'resourceType': 'Bundle', 'type': 'collection'}"
    return parser.parseResource(jsonAsString) as Bundle
}

fun testMedicationDispenseBundle(): MedicationDispense {
    return getMedicationDispenseFromAssetFileName("medication_dispense.json")
}

fun listOfUIPrescriptions() =
    listOf(testUIPrescription())

fun testUIPrescription() = UIPrescriptionOrder("taskId", "title", false, "accessCode")

fun communicationShipment() =
    Communication(
        "id",
        CommunicationProfile.ErxCommunicationReply,
        "time",
        "taskId",
        "telematiksId",
        "kbvUserId",
        "{\"version\": \"1\",\"supplyOptionsType\": \"shipment\",\"info_text\": \"Wir möchten Sie informieren, dass Ihre bestellten Medikamente versandt wurde!\",\"url\": \"das-e-rezept-fuer-deutschland.de\"}",
        false
    )

fun communicationDelivery() =
    Communication(
        "id",
        CommunicationProfile.ErxCommunicationReply,
        "time",
        "taskId",
        "telematiksId",
        "kbvUserId",
        "{\"version\": \"1\",\"supplyOptionsType\": \"delivery\",\"info_text\": \"\"}",
        false
    )

fun errorCommunicationDelivery() =
    Communication(
        "id",
        CommunicationProfile.ErxCommunicationReply,
        "time",
        "taskId",
        "telematiksId",
        "kbvUserId",
        "this payload is wrong",
        false
    )

@Throws(IOException::class)
fun readJsonFile(filename: String): String {
    return File(ASSET_BASE_PATH + filename).readText(Charsets.UTF_8)
}
