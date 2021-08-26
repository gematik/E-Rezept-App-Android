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
        coEvery { remoteDataSource.fetchTasks(lastModified) } answers {
            Result.Success(
                taskWithoutKBVBundle
            )
        }
        coEvery { remoteDataSource.allAuditEvents(lastModified) } answers {
            Result.Success(
                allAuditEvents
            )
        }
        coEvery { remoteDataSource.taskWithKBVBundle(any()) } answers {
            Result.Success(
                taskWithBundle()
            )
        }
        coEvery { remoteDataSource.fetchCommunications() } coAnswers { Result.Error(IOException()) }

        coEvery { localDataSource.saveAuditEvents(any()) } answers { nothing }
        coEvery { localDataSource.saveTask(any()) } answers { nothing }
        every { localDataSource.lastModifyTaskDate } answers { lastModified.toEpochSecond() }
        every { localDataSource.lastModifyTaskDate = 1618826574 } answers { }
        coEvery { localDataSource.deleteLowDetailEvents(any()) } answers { nothing }
    }

    @Test
    fun `if download tasks gets called - ensure that complete tasks are saved together with fresh audit events`() =
        coroutineRule.testDispatcher.runBlockingTest {
            prescriptionRepository.downloadTasks()

            coVerify(exactly = taskWithoutKBVBundle.entry.size) { localDataSource.saveTask(any()) }
            coVerify(exactly = 1) { localDataSource.saveAuditEvents(any()) }
            verify { localDataSource.lastModifyTaskDate = 1618826574 }
        }
}
