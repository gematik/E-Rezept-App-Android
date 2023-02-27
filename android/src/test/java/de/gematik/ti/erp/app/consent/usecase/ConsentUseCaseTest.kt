/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.consent.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.fhir.model.json
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import retrofit2.Response
import java.net.HttpURLConnection
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val consent = """{
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

class ConsentUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    private lateinit var consentRepository: ConsentRepository

    @MockK
    private lateinit var consentUseCase: ConsentUseCase

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)
        consentUseCase = spyk(
            ConsentUseCase(consentRepository)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get charge consent - consent granted`() {
        val consent = json.parseToJsonElement(consent)
        coEvery { consentRepository.getConsent("0") } returns Result.success(consent)
        runTest {
            val consentGranted = consentUseCase.getChargeConsent("0")
            assertEquals(true, consentGranted.getOrThrow())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get charge consent - success consent not granted`() {
        val consent = json.parseToJsonElement(
            """{}"""
        )
        coEvery { consentRepository.getConsent("0") } returns Result.success(consent)
        runTest {
            val consentGranted = consentUseCase.getChargeConsent("0")
            assertEquals(false, consentGranted.getOrThrow())
        }
    }

    @Test(expected = ApiCallException::class)
    fun `get charge consent - error conflict should return consent granted  `() {
        coEvery { consentRepository.getConsent("0") } returns Result.failure(
            ApiCallException(
                "",
                Response.error<HttpURLConnection>(HttpURLConnection.HTTP_CONFLICT, "".toResponseBody())
            )
        )
        runTest {
            val consentGranted = consentUseCase.getChargeConsent("0")
            assertEquals(true, consentGranted.getOrThrow())
        }
    }

    @Test
    fun `revoke charge consent - error not found should return consent already revoked  `() {
        runTest {
            coEvery { consentRepository.deleteChargeConsent("0") } returns Result.failure(
                ApiCallException(
                    "",
                    Response.error<HttpURLConnection>(HttpURLConnection.HTTP_NOT_FOUND, "".toResponseBody())
                )
            )
            val result = consentUseCase.deleteChargeConsent("0")
            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `revoke charge consent - success `() {
        runTest {
            coEvery { consentRepository.deleteChargeConsent("0") } returns Result.success(Unit)
            val result = consentUseCase.deleteChargeConsent("0")
            assertTrue(result.isSuccess)
        }
    }
}
