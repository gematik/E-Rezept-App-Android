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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.api.FhirPagination
import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.api.model.SaveTaskResult
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskEntryParserResultErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskPayloadErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.parser.TaskMedicationDispenseParser
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMetaDataPayloadErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FirTaskKbvPayloadErpModel
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleSeparationParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionParsers
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEntryParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMedicalDataParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMetadataParser
import de.gematik.ti.erp.app.fhir.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.prescription.remote.TaskRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DefaultTaskSyncRepository].
 */
class DefaultTaskSyncRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    private val remoteDataSource: TaskRemoteDataSource = mockk()
    private val taskLocalDataSource: TaskLocalDataSource = mockk()
    private val euTaskLocalDataSource: EuTaskLocalDataSource = mockk()

    // Use the real FhirPagination — it simply calls onFirstPage() then onPage()
    private val paginator = FhirPagination()

    private val taskEntryParser: TaskEntryParser = mockk()
    private val taskBundleSeparationParser: TaskBundleSeparationParser = mockk()
    private val taskMetadataParser: TaskMetadataParser = mockk()
    private val taskMedicalDataParser: TaskMedicalDataParser = mockk()
    private val taskDispenseParser: TaskMedicationDispenseParser = mockk()

    private val parsers = TaskEPrescriptionParsers(
        taskEntryParser = taskEntryParser,
        taskBundleSeparationParser = taskBundleSeparationParser,
        taskMetadataParser = taskMetadataParser,
        taskMedicalDataParser = taskMedicalDataParser,
        taskDispenseParser = taskDispenseParser
    )

    private lateinit var repository: DefaultTaskSyncRepository

    // Minimal JSON elements used as stand-ins across parser layers
    private val dummyBundle: JsonElement = JsonPrimitive("bundle")
    private val dummyTaskBundle: JsonElement = JsonPrimitive("task")
    private val dummyKbvBundle: JsonElement = JsonPrimitive("kbv")

    @Before
    fun setUp() {
        repository = DefaultTaskSyncRepository(
            remoteDataSource = remoteDataSource,
            taskLocalDataSource = taskLocalDataSource,
            euTaskLocalDataSource = euTaskLocalDataSource,
            parsers = parsers,
            paginator = paginator,
            dispatcher = dispatcher
        )

        // Default stubs shared across tests
        every { taskLocalDataSource.getLatestTaskModifiedTimestamp(any()) } returns flowOf(null)
        coEvery { taskLocalDataSource.saveSyncedTaskMetaData(any(), any()) } returns Result.success(Unit)
        coEvery { taskLocalDataSource.saveSyncedTaskMedicationDispense(any(), any()) } just runs
        coEvery { euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(any(), any(), any()) } just runs

        // Stub remote getTasks to return a single-page bundle (no next-page URL)
        coEvery { remoteDataSource.getTasks(any(), any(), any(), any()) } returns
            Result.success(dummyBundle)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Stubs the task-entry, bundle-separation, and metadata parsers to produce a single
     * completed task entry that triggers downloadMedicationDispenses.
     */
    private fun stubParsersForCompletedTask(
        taskId: String = TASK_ID,
        profileId: String = PROFILE_ID
    ) {
        // Page contains one task entry; nextPageUrl = null → single page
        every { taskEntryParser.extract(any()) } returns FhirTaskEntryParserResultErpModel(
            bundleTotal = 1,
            taskEntries = listOf(
                FhirTaskEntryDataErpModel(
                    id = taskId,
                    status = FhirTaskStatusErpModel.Ready,
                    lastModified = FhirTemporal.Instant(Instant.parse("2025-01-01T00:00:00Z"))
                )
            ),
            nextPageUrl = null
        )

        // Remote: fetch KBV + task metadata bundle for this task
        coEvery { remoteDataSource.taskWithKBVBundle(profileId, taskId) } returns
            Result.success(dummyBundle)

        // Separation parser produces taskBundle + kbvBundle wrappers
        every { taskBundleSeparationParser.extract(any()) } returns FhirTaskPayloadErpModel(
            taskBundle = FhirTaskMetaDataPayloadErpModel(dummyTaskBundle),
            kbvBundle = FirTaskKbvPayloadErpModel(dummyKbvBundle)
        )

        // Metadata parser returns a non-null model
        every { taskMetadataParser.extract(any()) } returns mockk(relaxed = true)

        // Medical-data parser: complete task with no missing properties
        val mockMedicalData = mockk<FhirTaskDataErpModel> {
            every { getMissingProperties() } returns emptyList()
        }
        every { taskMedicalDataParser.extract(any()) } returns mockMedicalData

        // saveSyncedTaskKBVData returns isCompleted = true → triggers dispense download
        coEvery {
            taskLocalDataSource.saveSyncedTaskKBVData(taskId, mockMedicalData)
        } returns Result.success(
            SaveTaskResult(
                isCompleted = true,
                lastModified = Instant.parse("2025-01-01T00:00:00Z")
            )
        )
    }

    /**
     * Stubs the dispense remote call and the dispense parser to return a collection built
     * from the given (possibly null) country codes.
     */
    private fun stubDispenseBundle(vararg countryCodeOrNull: String?) {
        coEvery {
            remoteDataSource.loadBundleOfMedicationDispenses(any(), any())
        } returns Result.success(dummyBundle)

        every { taskDispenseParser.extract(any()) } returns FhirMedicationDispenseErpModelCollection(
            dispensedMedications = countryCodeOrNull.mapIndexed { index, ccOrNull ->
                FhirMedicationDispenseErpModel(
                    dispenseId = "dispense-$index",
                    patientId = "patient-$index",
                    substitutionAllowed = false,
                    dosageInstruction = null,
                    performer = null,
                    handedOver = null,
                    dispensedDeviceRequest = null,
                    euCountryCode = ccOrNull
                )
            }
        )
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `downloadTasks - dispense with EU country code - addRedeemedEventIfValidOrderExists called with correct args`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()
            stubDispenseBundle(EU_COUNTRY_CODE)

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 1) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(
                    profileId = PROFILE_ID,
                    countryCode = EU_COUNTRY_CODE,
                    taskId = TASK_ID
                )
            }
        }

    @Test
    fun `downloadTasks - dispense without EU country code - addRedeemedEventIfValidOrderExists NOT called`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()
            stubDispenseBundle(null) // null → no EU code

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 0) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(any(), any(), any())
            }
        }

    @Test
    fun `downloadTasks - multiple dispenses mixed EU and non-EU - called only for EU dispenses`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()
            stubDispenseBundle(EU_COUNTRY_CODE, null)

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 1) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(
                    profileId = PROFILE_ID,
                    countryCode = EU_COUNTRY_CODE,
                    taskId = TASK_ID
                )
            }
        }

    @Test
    fun `downloadTasks - multiple EU country codes - called once per EU dispense`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()
            stubDispenseBundle("DE", "FR", "IT")

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 1) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(PROFILE_ID, "DE", TASK_ID)
            }
            coVerify(exactly = 1) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(PROFILE_ID, "FR", TASK_ID)
            }
            coVerify(exactly = 1) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(PROFILE_ID, "IT", TASK_ID)
            }
        }

    @Test
    fun `downloadTasks - dispense parser returns null collection - addRedeemedEventIfValidOrderExists NOT called`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()

            coEvery { remoteDataSource.loadBundleOfMedicationDispenses(any(), any()) } returns
                Result.success(dummyBundle)
            every { taskDispenseParser.extract(any()) } returns null

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 0) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(any(), any(), any())
            }
        }

    @Test
    fun `downloadTasks - empty dispense list - addRedeemedEventIfValidOrderExists NOT called`() =
        runTest(dispatcher) {
            stubParsersForCompletedTask()
            stubDispenseBundle() // empty vararg → no dispenses

            repository.downloadTasks(PROFILE_ID)

            coVerify(exactly = 0) {
                euTaskLocalDataSource.addRedeemedEventIfValidOrderExists(any(), any(), any())
            }
        }

    companion object {
        private const val PROFILE_ID = "profile-test-123"
        private const val TASK_ID = "task-test-abc"
        private const val EU_COUNTRY_CODE = "DE"
    }
}
