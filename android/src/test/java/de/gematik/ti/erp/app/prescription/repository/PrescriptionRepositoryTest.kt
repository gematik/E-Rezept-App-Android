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

package de.gematik.ti.erp.app.prescription.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ca.uhn.fhir.context.FhirContext
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.allAuditEvents
import de.gematik.ti.erp.app.utils.emptyAuditEvents
import de.gematik.ti.erp.app.utils.taskWithBundle
import de.gematik.ti.erp.app.utils.taskWithoutKBVBundle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

    var lastModified = LocalDateTime.ofEpochSecond(
        OffsetDateTime.now().toEpochSecond(), 0,
        ZoneOffset.UTC
    ).atOffset(ZoneOffset.UTC)

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
        coEvery { remoteDataSource.fetchTasks(lastModified, any()) } answers {
            Result.Success(
                taskWithoutKBVBundle
            )
        }
        coEvery { remoteDataSource.allAuditEvents(any(), lastModified, null, null) } answers {
            Result.Success(
                allAuditEvents
            )
        }
        coEvery { remoteDataSource.taskWithKBVBundle(any(), any()) } answers {
            Result.Success(
                taskWithBundle()
            )
        }
        coEvery { remoteDataSource.fetchCommunications(any()) } coAnswers { Result.Error(IOException()) }

        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coEvery { localDataSource.saveTask(any()) } answers { nothing }
        every { localDataSource.lastModifiedTaskDate(any()) } answers { lastModified.toEpochSecond() }
        every { localDataSource.setLastModifiedTaskDate(any(), 1618826574) } answers { }
        coEvery { localDataSource.deleteLowDetailEvents(any()) } answers { nothing }
        coEvery { localDataSource.auditEventsSyncedUpTo(any()) } answers { lastModified }
    }

    @Test
    fun `if download tasks gets called - ensure that complete tasks are saved together with fresh audit events`() =
        coroutineRule.testDispatcher.runBlockingTest {
            prescriptionRepository.downloadTasks("")
            prescriptionRepository.downloadAuditEvents("")
            coVerify(exactly = taskWithoutKBVBundle.entry.size) { localDataSource.saveTask(any()) }
            coVerify(exactly = 1) { localDataSource.saveAuditEvents(any()) }
            verify { localDataSource.setLastModifiedTaskDate(any(), 1618826574) }
        }

    @Test
    fun `download auditEvents - stores synced up time when all are downloaded`() {
        val emptyAuditEvents = emptyAuditEvents()
        coEvery { localDataSource.auditEventsSyncedUpTo(any()) } returns Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC)
        every { localDataSource.storeAuditEventSyncError() } answers { nothing }
        coEvery { localDataSource.setAllAuditEventsSyncedUpTo(any()) } answers { nothing }
        coEvery { remoteDataSource.allAuditEvents(any(), any()) } answers {
            Result.Success(
                emptyAuditEvents
            )
        }
        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coroutineRule.testDispatcher.runBlockingTest {
            val result = prescriptionRepository.downloadAuditEvents("") as Result.Success<String>
            assert(result.data.isEmpty())
            coVerify(exactly = 1) { localDataSource.setAllAuditEventsSyncedUpTo(any()) }
        }
    }

    @Test
    fun `download auditEvents - provides link for next page of auditEvents`() {
        coEvery { localDataSource.auditEventsSyncedUpTo(any()) } returns Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC)
        every { localDataSource.storeAuditEventSyncError() } answers { nothing }
        coEvery { localDataSource.setAllAuditEventsSyncedUpTo(any()) } answers { nothing }
        coEvery { remoteDataSource.allAuditEvents(any(), any()) } answers {
            Result.Success(
                allAuditEvents
            )
        }
        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coroutineRule.testDispatcher.runBlockingTest {
            val result = prescriptionRepository.downloadAuditEvents("") as Result.Success<String>
            assert(result.data.isNotEmpty())
            coVerify(exactly = 0) { localDataSource.setAllAuditEventsSyncedUpTo(any()) }
        }
    }

    @Test
    fun `download auditEvents - returns error and therefore does not store synced up time`() {
        coEvery { localDataSource.auditEventsSyncedUpTo(any()) } returns Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC)
        every { localDataSource.storeAuditEventSyncError() } answers { nothing }
        coEvery { localDataSource.setAllAuditEventsSyncedUpTo(any()) } answers { nothing }
        coEvery { remoteDataSource.allAuditEvents(any(), any()) } answers {
            Result.Error(Exception("testException"))
        }
        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coroutineRule.testDispatcher.runBlockingTest {
            prescriptionRepository.downloadAuditEvents("") as Result.Error
            coVerify(exactly = 0) { localDataSource.setAllAuditEventsSyncedUpTo(any()) }
            coVerify(exactly = 1) { localDataSource.storeAuditEventSyncError() }
        }
    }
}
