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
import de.gematik.ti.erp.app.fhir.constant.consent.ConsentConstants
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement

/**
 * Builder for ERP Consent requests (PKV and EU).
 * Uses ConsentConstants sealed class for type-safe access.
 *
 * ## Version Selection (Debug only):
 * The `erpChargeVersion` parameter allows selecting specific ERP Charge consent versions
 * in debug builds. In production, this parameter is ignored and always uses V1_1.
 */
object ConsentRequest {

    /**
     * Creates a FHIR consent request.
     *
     * @param patientId The patient's insurance identifier (KVNR)
     * @param category The consent category (EUCONSENT or PKVCONSENT)
     * @param timeStamp The consent timestamp (defaults to now)
     * @param erpChargeVersion The ERP Charge version to use (DEBUG ONLY, ignored in production)
     * @return FHIR Consent JSON element ready for submission
     */
    fun createConsentRequest(
        patientId: String,
        category: String,
        timeStamp: String = Clock.System.now().asFhirTemporal().formattedString(),
        erpChargeVersion: ConsentConstants.ErpCharge = ConsentConstants.ErpCharge.DEFAULT
    ): JsonElement {
        val constants = if (category == ConsentCategory.EUCONSENT.code) {
            ConsentConstants.ErpEu()
        } else {
            erpChargeVersion
        }

        val request = FhirConsentModel(
            resourceType = "Consent",
            id = constants.consentId,
            meta = FhirMeta(
                profile = listOf(constants.profileUrl)
            ),
            status = ConsentConstants.STATUS_ACTIVE,
            patient = FhirPatientRef(
                identifier = FhirIdentifier(
                    system = constants.patientSystem,
                    value = patientId
                )
            ),
            dateTime = timeStamp,
            scope = FhirCodeableConcept(
                coding = listOf(
                    FhirCoding(
                        system = ConsentConstants.SCOPE_SYSTEM,
                        code = ConsentConstants.SCOPE_CODE_PATIENT_PRIVACY,
                        display = ConsentConstants.SCOPE_DISPLAY_PRIVACY_CONSENT
                    )
                )
            ),
            category = listOf(
                FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = constants.categorySystem,
                            code = constants.categoryCode,
                            display = constants.categoryDisplay
                        )
                    )
                )
            ),
            policyRule = FhirCodeableConcept(
                coding = listOf(
                    FhirCoding(
                        system = ConsentConstants.POLICY_RULE_SYSTEM,
                        code = ConsentConstants.POLICY_RULE_CODE_OPTIN
                    )
                )
            )
        )

        val jsonString = SafeJson.value.encodeToString(FhirConsentModel.serializer(), request)
        return SafeJson.value.parseToJsonElement(jsonString)
    }
}
