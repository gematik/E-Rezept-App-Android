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

package de.gematik.ti.erp.app.fhir.consent.model

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirCoding
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirConsentModel
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirMeta
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirPatientRef
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.consent.ErpEuConsentConst
import kotlinx.serialization.json.JsonElement

object EuConsentRequest {
    fun createConsentRequest(
        patientId: String
    ): JsonElement {
        val request = FhirConsentModel(
            resourceType = ErpEuConsentConst.RESOURCE_TYPE,
            id = ErpEuConsentConst.CONSENT_ID,
            meta = FhirMeta(
                profile = listOf(ErpEuConsentConst.PROFILE_URL)
            ),
            status = ErpEuConsentConst.STATUS_ACTIVE,
            patient = FhirPatientRef(
                identifier = FhirIdentifier(
                    system = ErpEuConsentConst.PATIENT_SYSTEM_GKV_KVID10,
                    value = patientId
                )
            ),
            scope = FhirCodeableConcept(
                coding = listOf(
                    FhirCoding(
                        system = ErpEuConsentConst.SCOPE_SYSTEM,
                        code = ErpEuConsentConst.SCOPE_CODE_PATIENT_PRIVACY,
                        display = ErpEuConsentConst.SCOPE_DISPLAY_PRIVACY_CONSENT
                    )
                )
            ),
            category = listOf(
                FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = ErpEuConsentConst.CATEGORY_SYSTEM,
                            code = ErpEuConsentConst.CATEGORY_CODE_EUDISPCONS,
                            display = ErpEuConsentConst.CATEGORY_DISPLAY_EU_REDEEM
                        )
                    )
                )
            ),
            policyRule = FhirCodeableConcept(
                coding = listOf(
                    FhirCoding(
                        system = ErpEuConsentConst.POLICY_RULE_SYSTEM,
                        code = ErpEuConsentConst.POLICY_RULE_CODE_OPTIN
                    )
                )
            )
        )

        val jsonString = SafeJson.value.encodeToString(FhirConsentModel.serializer(), request)
        return SafeJson.value.parseToJsonElement(jsonString)
    }
}
