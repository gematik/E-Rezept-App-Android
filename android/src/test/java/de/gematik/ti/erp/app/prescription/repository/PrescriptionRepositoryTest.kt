/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ca.uhn.fhir.context.FhirContext
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.allAuditEvents
import de.gematik.ti.erp.app.utils.emptyAuditEvents
import de.gematik.ti.erp.app.utils.taskWithBundle
import de.gematik.ti.erp.app.utils.taskWithoutKBVBundle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PrescriptionRepositoryTest {

    private lateinit var prescriptionRepository: PrescriptionRepository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var localDataSource: LocalDataSource

    @MockK
    lateinit var remoteDataSource: RemoteDataSource

    var lastModifiedTask = Instant.now()
    var lastModifiedAudit = OffsetDateTime.now()

    var mapper: Mapper = Mapper(FhirContext.forR4().newJsonParser())

    private val taskWithoutKBVBundle = taskWithoutKBVBundle()
    private val allAuditEvents = allAuditEvents()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        prescriptionRepository = PrescriptionRepository(
            coroutineRule.testDispatchProvider,
            localDataSource,
            remoteDataSource,
            mapper
        )
        coEvery { remoteDataSource.fetchTasks(lastModifiedTask, any()) } answers {
            Result.success(
                taskWithoutKBVBundle
            )
        }
        coEvery { remoteDataSource.allAuditEvents(any(), lastModifiedAudit, null, null) } answers {
            Result.success(
                allAuditEvents
            )
        }
        coEvery { remoteDataSource.taskWithKBVBundle(any(), any()) } answers {
            Result.success(
                taskWithBundle()
            )
        }
        coEvery { remoteDataSource.fetchCommunications(any()) } coAnswers { Result.failure(IOException()) }

        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coEvery { localDataSource.saveTask(any()) } answers { nothing }
        coEvery { localDataSource.taskSyncedUpTo(any()) } answers { lastModifiedTask }
        coEvery { localDataSource.updateTaskSyncedUpTo(any(), any()) } answers { }
        coEvery { localDataSource.deleteLowDetailEvents(any()) } answers { nothing }
        coEvery { localDataSource.auditEventsSyncedUpTo(any()) } answers { lastModifiedAudit }
    }

    @Test
    fun `if download tasks gets called - ensure that complete tasks are saved`() =
        runTest {
            val emptyAuditEvents = emptyAuditEvents()

            coEvery { localDataSource.auditEventsSyncedUpTo(any()) } returns Instant.ofEpochSecond(0)
                .atOffset(ZoneOffset.UTC)
            coEvery { remoteDataSource.allAuditEvents(any(), any()) } answers {
                Result.success(
                    emptyAuditEvents
                )
            }
            prescriptionRepository.downloadTasks("")
            coVerify(exactly = taskWithoutKBVBundle.entry.size) { localDataSource.saveTask(any()) }
            coVerify { localDataSource.updateTaskSyncedUpTo(any(), any()) }
        }

    @Test
    fun `download auditEvents - stores synced up time each page`() {
        val timestamp = OffsetDateTime.parse("2022-01-03T09:11:30+02:00")
        val profileName = "Test"
        coEvery { localDataSource.auditEventsSyncedUpTo(profileName) } returns timestamp
        coEvery { localDataSource.setAllAuditEventsSyncedUpTo(profileName) } answers { }

        coEvery {
            remoteDataSource.allAuditEvents(
                profileName = profileName,
                lastKnownUpdate = timestamp,
                count = 50,
                offset = null
            )
        } answers {
            Result.success(allAuditEvents())
        } andThenAnswer {
            Result.success(emptyAuditEvents())
        }
        coEvery { localDataSource.saveAuditEvents(any()) } answers { }

        runTest {
            prescriptionRepository.downloadAllAuditEvents(profileName)
        }

        coVerify(exactly = 2) { localDataSource.setAllAuditEventsSyncedUpTo(profileName) }
    }

    @Test
    fun `failed to download auditEvents - doesn't save any audits`() {
        val timestamp = OffsetDateTime.parse("2022-01-03T09:11:30+02:00")
        val profileName = "Test"
        coEvery { localDataSource.auditEventsSyncedUpTo(profileName) } returns timestamp
        coEvery { localDataSource.setAllAuditEventsSyncedUpTo(profileName) } answers { }

        coEvery {
            remoteDataSource.allAuditEvents(
                profileName = profileName,
                lastKnownUpdate = timestamp,
                count = 50,
                offset = null
            )
        } answers {
            Result.failure(IllegalArgumentException(""))
        }

        runTest {
            prescriptionRepository.downloadAllAuditEvents(profileName)
        }

        coVerify(exactly = 0) { localDataSource.setAllAuditEventsSyncedUpTo(profileName) }
    }
}
