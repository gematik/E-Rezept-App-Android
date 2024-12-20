/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.consent.repository

import de.gematik.ti.erp.app.fhir.model.ResourceBasePath
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConsentRepositoryTest {

    @MockK(relaxed = true)
    private lateinit var localDataSource: ConsentLocalDataSource

    @MockK
    private lateinit var remoteDataSource: ConsentRemoteDataSource

    private lateinit var consentRepository: ConsentRepository

    private val consentNotGrantedJson by lazy { File("$ResourceBasePath/fhir/pkv/consent_not_granted.json").readText() }
    private val consentGrantedJson by lazy { File("$ResourceBasePath/fhir/pkv/consent_granted.json").readText() }

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        consentRepository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )

        coEvery { remoteDataSource.getConsent("0") } coAnswers {
            Result.success(Json.parseToJsonElement(consentNotGrantedJson))
        }

        coEvery { remoteDataSource.getConsent("1") } coAnswers {
            Result.success(Json.parseToJsonElement(consentGrantedJson))
        }
    }

    @Test
    fun `get consent granted should answer false`() {
        runTest {
            val result = consentRepository.getConsent("0").map {
                consentRepository.isConsentGranted(it)
                assertFalse { consentRepository.isConsentGranted(it) }
            }
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun `get consent granted should answer true`() {
        runTest {
            val result = consentRepository.getConsent("1").map {
                consentRepository.isConsentGranted(it)
                assertTrue { consentRepository.isConsentGranted(it) }
            }
            assertTrue(result.isSuccess)
        }
    }
}
