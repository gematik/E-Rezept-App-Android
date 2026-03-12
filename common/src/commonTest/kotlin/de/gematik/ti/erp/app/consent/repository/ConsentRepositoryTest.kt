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

package de.gematik.ti.erp.app.consent.repository

import de.gematik.ti.erp.app.fhir.consent.FhirConsentParser
import de.gematik.ti.erp.app.fhir.model.ResourceBasePath
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.json.Json
import org.junit.Before
import java.io.File

class ConsentRepositoryTest {

    @MockK(relaxed = true)
    private lateinit var localDataSource: ConsentLocalDataSource

    @MockK
    private lateinit var remoteDataSource: ConsentRemoteDataSource

    @MockK(relaxed = true)
    private lateinit var fhirConsentParser: FhirConsentParser

    private lateinit var consentRepository: ConsentRepository

    private val consentNotGrantedJson by lazy { File("$ResourceBasePath/fhir/pkv/pkv1_2/consent_not_granted.json").readText() }
    private val consentGrantedJson by lazy { File("$ResourceBasePath/fhir/pkv/pkv1_2/consent_granted.json").readText() }

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        consentRepository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            parsers = fhirConsentParser
        )

        coEvery { remoteDataSource.getConsent("0", category = any()) } coAnswers {
            Result.success(Json.parseToJsonElement(consentNotGrantedJson))
        }

        coEvery { remoteDataSource.getConsent("1", category = any()) } coAnswers {
            Result.success(Json.parseToJsonElement(consentGrantedJson))
        }
    }
}
