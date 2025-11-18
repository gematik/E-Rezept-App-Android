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

package de.gematik.ti.erp.app.fhir.euredeem

import de.gematik.ti.erp.app.data.euRedeemAccessCodeRequestV1
import de.gematik.ti.erp.app.data.euRedeemAccessCodeResponseV1
import de.gematik.ti.erp.app.data.euRedeemTaskPatchInputRequestV1
import de.gematik.ti.erp.app.fhir.euredeem.mocks.euRedeemAccessCodeResponseErpModel_v1_0
import de.gematik.ti.erp.app.fhir.euredeem.mocks.euRedeemAccessCodeResponse_v1_0
import de.gematik.ti.erp.app.fhir.euredeem.model.FhirEuRedeemAccessCodeResponseModel.Companion.toFhirEuRedeemAccessCodeResponseModel
import de.gematik.ti.erp.app.fhir.euredeem.model.accessCodeRegex
import de.gematik.ti.erp.app.fhir.euredeem.model.createEuRedeemAccessCodePayload
import de.gematik.ti.erp.app.fhir.euredeem.model.createIsEuRedeemableByPatientAuthorizationPayload
import de.gematik.ti.erp.app.fhir.euredeem.model.generateAccessCode
import de.gematik.ti.erp.app.fhir.euredeem.parser.EuRedeemAccessCodeResponseParser
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class EuRedeemAccessCodeParserTest {

    val parser = EuRedeemAccessCodeResponseParser()

    @Test
    fun `test euRedeemAccessCodeResponse parsing`() = runTest {
        val bundle = Json.parseToJsonElement(euRedeemAccessCodeResponseV1)
        val result = bundle.toFhirEuRedeemAccessCodeResponseModel()
        val erpModel = parser.extract(bundle)
        assertEquals(euRedeemAccessCodeResponse_v1_0, result)
        assertEquals(euRedeemAccessCodeResponseErpModel_v1_0, erpModel)
    }

    @Test
    fun `test euRedeemAccessCodeRequest creation`() = runTest {
        val jsonRequest = Json.parseToJsonElement(euRedeemAccessCodeRequestV1)
        val accessCode = generateAccessCode()
        assertEquals(6, accessCode.filter { accessCodeRegex.matches(it.toString()) }.length)
        val payload = createEuRedeemAccessCodePayload("BE", "aBC123")
        assertEquals(payload, jsonRequest)
    }

    @Test
    fun `test isEuRedeemableByPatientAuthorizationRequest creation`() = runTest {
        val jsonRequest = Json.parseToJsonElement(euRedeemTaskPatchInputRequestV1)
        val payload = createIsEuRedeemableByPatientAuthorizationPayload(true)
        assertEquals(payload, jsonRequest)
    }
}
