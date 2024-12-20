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

package de.gematik.ti.erp.app.consent.usecases

import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetConsentUseCaseTest {

    private val dispatcher = StandardTestDispatcher()
    private val remoteDataSource = mockk<ConsentRemoteDataSource>()
    private val localDataSource = mockk<ConsentLocalDataSource>()
    private lateinit var repository: ConsentRepository
    private lateinit var useCase: GetConsentUseCase

    @Before
    fun setup() {
        repository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )
        useCase = GetConsentUseCase(repository)
    }

    @Test
    fun `on get consent successfully for a profile`() {
        coEvery { remoteDataSource.getConsent(any()) } returns Result.success(
            json.parseToJsonElement(MOCK_JSON_RESPONSE_CONSENT)
        )
        runTest(dispatcher) {
            val result = useCase.invoke(profileId).first()
            assertEquals(ConsentState.ValidState.Granted(ConsentContext.GetConsent), result)
            coVerify(exactly = 1) {
                repository.getConsent(profileId)
            }
        }
    }

    @Test
    fun `on get consent failed for a profile`() {
        coEvery { remoteDataSource.getConsent(any()) } returns Result.failure(Throwable("server error"))
        runTest(dispatcher) {
            val result = useCase.invoke(profileId).first()
            assertEquals(ConsentState.ConsentErrorState.Unknown, result)
            coVerify(exactly = 1) {
                repository.getConsent(profileId)
            }
        }
    }

    companion object {
        private const val profileId = "7fo98w-43tgv-23w"

        private val json = Json {
            encodeDefaults = true
            prettyPrint = false
        }

        private val MOCK_JSON_RESPONSE_CONSENT = """{
  "id": "4af9d0b8-7d90-4606-ae3d-12a45a148ff7",
  "type": "searchset",
  "timestamp": "2023-02-06T08:55:38.043+00:00",
  "resourceType": "Bundle",
  "total": 0,
  "entry": [
    {
      "fullUrl": "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/Consent/CHARGCONS-X764228532",
      "resource": {
        "resourceType": "Consent",
        "id": "CHARGCONS-X764228532",
        "meta": {
          "profile": [
            "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent|1.0"
          ]
        },
        "status": "active",
        "scope": {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/consentscope",
              "code": "patient-privacy",
              "display": "Privacy Consent"
            }
          ]
        },
        "category": [
          {
            "coding": [
              {
                "system": "https://gematik.de/fhir/erpchrg/CodeSystem/GEM_ERPCHRG_CS_ConsentType",
                "code": "CHARGCONS",
                "display": "Consent for saving electronic charge item"
              }
            ]
          }
        ],
        "patient": {
          "identifier": {
            "system": "http://fhir.de/sid/pkv/kvid-10",
            "value": "X764228532"
          }
        },
        "dateTime": "2023-02-03T13:19:04.642+00:00",
        "policyRule": {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
              "code": "OPTIN"
            }
          ]
        }
      },
      "search": {
        "mode": "match"
      }
    }
  ]
}
        """.trimIndent()
    }
}
